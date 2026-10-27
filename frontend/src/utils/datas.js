// A API usa dd/MM/yyyy; <input type="date"> usa yyyy-MM-dd.
export function paraApi(iso) {
  if (!iso) return '';
  const [a, m, d] = iso.split('-');
  return `${d}/${m}/${a}`;
}

export function deApi(valor) {
  if (!valor) return '';
  if (valor.includes('/')) {
    const [d, m, a] = valor.split('/');
    return `${a}-${m}-${d}`;
  }
  return valor.slice(0, 10); // já veio em ISO
}
