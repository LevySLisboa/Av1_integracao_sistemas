import { useEffect, useState } from 'react';
import { Anchor, Button, Group, PinInput, Stack, Text } from '@mantine/core';
import { api } from '../api.js';
import ErroAlert from './ErroAlert.jsx';

const INTERVALO_REENVIO = 60; // igual ao app.twofa.reenvio-cooldown-segundos do back-end

function useContagem(inicial) {
  const [restante, setRestante] = useState(inicial);
  useEffect(() => {
    if (restante <= 0) return undefined;
    const t = setTimeout(() => setRestante((r) => r - 1), 1000);
    return () => clearTimeout(t);
  }, [restante]);
  return [restante, setRestante];
}

const mmss = (s) => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;

export default function EtapaCodigo({ info, onSucesso, onVoltar }) {
  const [codigo, setCodigo] = useState('');
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);
  const [expira, setExpira] = useContagem(info.codigoExpiraEmSegundos);
  const [espera, setEspera] = useContagem(INTERVALO_REENVIO);

  async function verificar(valor) {
    setErro(null);
    setCarregando(true);
    try {
      const resposta = await api.verificar(valor);
      onSucesso(resposta.usuario);
    } catch (err) {
      setErro(err);
      setCodigo('');
    } finally {
      setCarregando(false);
    }
  }

  async function reenviar() {
    setErro(null);
    try {
      const resposta = await api.reenviar();
      setExpira(resposta.codigoExpiraEmSegundos);
      setEspera(INTERVALO_REENVIO);
      setCodigo('');
    } catch (err) {
      setErro(err);
    }
  }

  return (
    <Stack gap="md">
      <Text size="sm">{info.mensagem}</Text>

      <Stack gap={6} align="center" py="xs">
        <PinInput
          length={6}
          type="number"
          size="lg"
          oneTimeCode
          autoFocus
          value={codigo}
          onChange={setCodigo}
          onComplete={verificar}
          disabled={carregando || expira <= 0}
          aria-label="Código de 6 dígitos"
        />
        <Text size="xs" c={expira > 0 ? 'dimmed' : 'red'}>
          {expira > 0 ? `O código expira em ${mmss(expira)}` : 'Código expirado. Peça um novo.'}
        </Text>
      </Stack>

      <ErroAlert erro={erro} />

      <Button onClick={() => verificar(codigo)} loading={carregando} disabled={codigo.length !== 6} fullWidth>
        Confirmar código
      </Button>

      <Group justify="space-between">
        <Anchor component="button" type="button" size="sm" onClick={onVoltar}>
          Voltar ao login
        </Anchor>
        <Anchor
          component="button"
          type="button"
          size="sm"
          onClick={reenviar}
          disabled={espera > 0}
          style={espera > 0 ? { opacity: 0.5, cursor: 'not-allowed' } : undefined}
        >
          {espera > 0 ? `Reenviar código em ${espera}s` : 'Reenviar código'}
        </Anchor>
      </Group>
    </Stack>
  );
}
