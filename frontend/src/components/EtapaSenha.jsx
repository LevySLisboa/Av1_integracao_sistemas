import { useState } from 'react';
import { Button, PasswordInput, Stack, Text, TextInput } from '@mantine/core';
import { api } from '../api.js';
import ErroAlert from './ErroAlert.jsx';

export default function EtapaSenha({ onSucesso }) {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function enviar(e) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const resposta = await api.login(email.trim(), senha);
      setSenha('');
      onSucesso(resposta, email.trim());
    } catch (err) {
      setErro(err);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <form onSubmit={enviar}>
      <Stack gap="md">
        <Text size="sm" c="dimmed">
          Informe seu email e sua senha. Se estiverem corretos, enviaremos um código de 6 dígitos para o seu email.
        </Text>
        <TextInput
          label="Email"
          type="email"
          autoComplete="username"
          value={email}
          onChange={(e) => setEmail(e.currentTarget.value)}
          required
          autoFocus
        />
        <PasswordInput
          label="Senha"
          autoComplete="current-password"
          value={senha}
          onChange={(e) => setSenha(e.currentTarget.value)}
          required
        />
        <ErroAlert erro={erro} />
        <Button type="submit" loading={carregando} fullWidth>
          Enviar código por email
        </Button>
      </Stack>
    </form>
  );
}
