import { useEffect, useState } from 'react';
import { Accordion, Box, Container, Loader, Paper, Center, Stack, Stepper, Text, Title } from '@mantine/core';
import { api } from './api.js';
import EtapaSenha from './components/EtapaSenha.jsx';
import EtapaCodigo from './components/EtapaCodigo.jsx';
import Painel from './components/Painel.jsx';

const PASSO = { senha: 0, codigo: 1, logado: 3 }; // 3 = todas as etapas concluídas

export default function App() {
  const [etapa, setEtapa] = useState('carregando'); // carregando | senha | codigo | logado
  const [info2fa, setInfo2fa] = useState(null);
  const [usuario, setUsuario] = useState(null);

  // Ao abrir (ou recarregar) a página, vê se já existe uma sessão autenticada.
  useEffect(() => {
    api
      .me()
      .then((u) => {
        setUsuario(u);
        setEtapa('logado');
      })
      .catch(() => setEtapa('senha'));
  }, []);

  return (
    <Box
      mih="100vh"
      py={{ base: 'md', sm: 48 }}
      style={{ background: 'light-dark(var(--mantine-color-tinta-0), var(--mantine-color-dark-8))' }}
    >
      <Container size={etapa === 'logado' && usuario?.papel === 'ADMIN' ? 1100 : 560}>
        <Stack gap="lg">
          <div>
            <Title order={1} size="h2">
              Sistema de Provas
            </Title>
            <Text c="dimmed" size="sm">
              Entrada com senha e código de confirmação por email.
            </Text>
          </div>

          {etapa !== 'carregando' && (
            <Stepper active={PASSO[etapa]} size="xs" wrap={false} allowNextStepsSelect={false}>
              <Stepper.Step label="Senha" />
              <Stepper.Step label="Código" />
              <Stepper.Step label="Acesso" />
            </Stepper>
          )}

          <Paper withBorder shadow="sm" p={{ base: 'md', sm: 'xl' }} radius="lg">
            {etapa === 'carregando' && (
              <Center py="xl">
                <Loader size="sm" />
              </Center>
            )}
            {etapa === 'senha' && (
              <EtapaSenha
                onSucesso={(resposta) => {
                  setInfo2fa(resposta);
                  setEtapa('codigo');
                }}
              />
            )}
            {etapa === 'codigo' && (
              <EtapaCodigo
                info={info2fa}
                onVoltar={() => setEtapa('senha')}
                onSucesso={(u) => {
                  setUsuario(u);
                  setEtapa('logado');
                }}
              />
            )}
            {etapa === 'logado' && (
              <Painel
                usuario={usuario}
                onSair={() => {
                  setUsuario(null);
                  setEtapa('senha');
                }}
              />
            )}
          </Paper>

          <Accordion variant="contained" radius="md">
            <Accordion.Item value="fluxo">
              <Accordion.Control>Como funciona o login</Accordion.Control>
              <Accordion.Panel>
                <Text size="sm">
                  1. A página pede um token CSRF (<code>GET /auth/csrf</code>).
                  <br />
                  2. Email e senha vão para <code>POST /auth/login</code>. Com a senha correta, o servidor cria uma
                  sessão ainda sem acesso e envia o código por email.
                  <br />
                  3. O código vai para <code>POST /auth/verificar-2fa</code>. Se estiver certo, a sessão passa a
                  ter acesso.
                  <br />
                  4. A cada requisição, o navegador envia o cookie de sessão (HttpOnly, que o JavaScript não lê).
                  <br />
                  5. <code>POST /auth/logout</code> destrói a sessão no servidor.
                </Text>
              </Accordion.Panel>
            </Accordion.Item>
          </Accordion>
        </Stack>
      </Container>
    </Box>
  );
}
