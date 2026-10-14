/**
 * DareDash: purchase verification endpoint for Cloudflare Workers (free plan possible).
 * Deploy this separately. Required secret: GOOGLE_SERVICE_ACCOUNT_JSON (full service account JSON).
 * Required env variable: PLAY_PACKAGE_NAME=com.daredash.party
 * Do not place service account credentials in app, GitHub or client-side JavaScript.
 */
const PRODUCTS = new Set(['all_packs', 'remove_ads']);
const NO_CACHE = { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' };
const reply = (body, status = 200) => new Response(JSON.stringify(body), { status, headers: NO_CACHE });
const b64url = x => btoa(String.fromCharCode(...new Uint8Array(x)))
  .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
function pemBytes(pem) {
  const base64 = pem.replace(/-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\s/g, '');
  return Uint8Array.from(atob(base64), x => x.charCodeAt(0));
}
async function googleToken(creds) {
  const now = Math.floor(Date.now() / 1000);
  const header = b64url(new TextEncoder().encode(JSON.stringify({ alg: 'RS256', typ: 'JWT' })));
  const payload = b64url(new TextEncoder().encode(JSON.stringify({
    iss: creds.client_email, scope: 'https://www.googleapis.com/auth/androidpublisher',
    aud: 'https://oauth2.googleapis.com/token', iat: now, exp: now + 3600
  })));
  const signingInput = `${header}.${payload}`;
  const key = await crypto.subtle.importKey('pkcs8', pemBytes(creds.private_key),
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' }, false, ['sign']);
  const signature = await crypto.subtle.sign('RSASSA-PKCS1-v1_5', key,
    new TextEncoder().encode(signingInput));
  const jwt = `${signingInput}.${b64url(signature)}`;
  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer', assertion: jwt })
  });
  if (!response.ok) throw new Error(`Google OAuth ${response.status}`);
  const data = await response.json();
  return data.access_token;
}

export default {
  async fetch(request, env) {
    const path = new URL(request.url).pathname;
    if (path === '/health' && request.method === 'GET')
      return reply({ status: 'ok', configured: Boolean(env.PLAY_PACKAGE_NAME && env.GOOGLE_SERVICE_ACCOUNT_JSON) });
    if (path !== '/verify' || request.method !== 'POST') return reply({ error: 'not_found' }, 404);
    if (!env.PLAY_PACKAGE_NAME || !env.GOOGLE_SERVICE_ACCOUNT_JSON)
      return reply({ verified: false, error: 'not_configured' }, 503);
    if (Number(request.headers.get('content-length') || 0) > 10000)
      return reply({ verified: false, error: 'too_large' }, 413);
    try {
      const { productId, purchaseToken } = await request.json();
      if (!PRODUCTS.has(productId) || typeof purchaseToken !== 'string' ||
          purchaseToken.length < 12 || purchaseToken.length > 4096)
        return reply({ verified: false, error: 'invalid_input' }, 400);
      const creds = JSON.parse(env.GOOGLE_SERVICE_ACCOUNT_JSON);
      const token = await googleToken(creds);
      const endpoint = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/` +
        `${encodeURIComponent(env.PLAY_PACKAGE_NAME)}/purchases/productsv2/tokens/` +
        `${encodeURIComponent(purchaseToken)}`;
      const result = await fetch(endpoint, { headers: { Authorization: `Bearer ${token}` } });
      if (!result.ok) return reply({ verified: false, error: 'not_verified' }, 403);
      const purchase = await result.json();
      const purchased = purchase.purchaseStateContext?.purchaseState === 'PURCHASED';
      const matchingItem = purchase.productLineItem?.find(item => item.productId === productId);
      // Refunded/voided transactions must not be granted: recent purchases may
      // remain PURCHASED briefly after a refund, so recheck with the Play API on restore.
      const refundableQty = matchingItem?.productOfferDetails?.refundableQuantity;
      const notFullyRefunded = refundableQty === undefined || refundableQty > 0;
      if (!purchased || !matchingItem || !notFullyRefunded)
        return reply({ verified: false, error: 'not_entitled' }, 403);
      return reply({ verified: true, productId });
    } catch (err) {
      return reply({ verified: false, error: 'verification_failed' }, 503);
    }
  }
};
