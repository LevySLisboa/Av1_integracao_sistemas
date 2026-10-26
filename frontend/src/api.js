// Cliente da API. Tudo passa por /api (proxy do Vite -> Spring).
// Autenticação = cookie de sessão (enviado pelo navegador) + token CSRF nos métodos que alteram dados.

const BASE = '/api';

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.mensagem || `Erro ${status}`);
    this.status = status;
    this.body = body;
  }
}

async function obterCsrf() {
  const r = await fetch(`${BASE}/auth/csrf`, { credentials: 'same-origin' });
  const j = await r.json();
  return { header: j.headerName, token: j.token };
}

/** Faz a chamada e devolve { status, ok, data } sem lançar erro (útil para mostrar 401/403). */
export async function bruto(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  try {
    if (method !== 'GET') {
      const csrf = await obterCsrf();
      headers[csrf.header] = csrf.token;
    }
    const res = await fetch(`${BASE}${path}`, {
      method,
      headers,
      credentials: 'same-origin',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    const texto = await res.text();
    let data = null;
    try {
      data = texto ? JSON.parse(texto) : null;
    } catch {
      data = texto;
    }
    return { status: res.status, ok: res.ok, data };
  } catch {
    return {
      status: 0,
      ok: false,
      data: { mensagem: 'Não foi possível conectar à API. Verifique se o back-end está no ar.' },
    };
  }
}

async function chamar(path, opcoes) {
  const r = await bruto(path, opcoes);
  if (!r.ok) throw new ApiError(r.status, r.data);
  return r.data;
}

export const api = {
  me: () => chamar('/auth/me'),
  login: (email, senha) => chamar('/auth/login', { method: 'POST', body: { email, senha } }),
  verificar: (codigo) => chamar('/auth/verificar-2fa', { method: 'POST', body: { codigo } }),
  reenviar: () => chamar('/auth/reenviar-codigo', { method: 'POST' }),
  logout: () => chamar('/auth/logout', { method: 'POST' }),
  testar: (path) => bruto(path),
};
