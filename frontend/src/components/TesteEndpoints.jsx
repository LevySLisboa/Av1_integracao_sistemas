import { useState } from 'react';
import { Badge, Button, Code, Group, Paper, ScrollArea, SimpleGrid, Text } from '@mantine/core';
import { api } from '../api.js';

const ENDPOINTS = [
  { rotulo: 'Provas', path: '/provas' },
  { rotulo: 'Alunos', path: '/alunos' },
  { rotulo: 'Professores', path: '/professor' },
  { rotulo: 'Usuários', path: '/usuarios' },
  { rotulo: 'Prova 1 + feriados (API externa)', path: '/integracao/provas/1/calendario' },
];

export default function TesteEndpoints() {
  const [resultado, setResultado] = useState(null);
  const [carregando, setCarregando] = useState(null);

  async function testar(path) {
    setCarregando(path);
    const r = await api.testar(path);
    setResultado({ path, ...r });
    setCarregando(null);
  }

  return (
    <>
      <Paper withBorder p="sm">
        <Text size="sm" fw={600} mb={6}>
          Testar endpoints protegidos
        </Text>
        <Text size="xs" c="dimmed" mb="sm">
          Cada botão faz um GET usando só o cookie de sessão. O que o seu perfil não pode acessar volta como 403.
        </Text>
        <SimpleGrid cols={{ base: 1, xs: 2 }} spacing="xs">
          {ENDPOINTS.map((e) => (
            <Button key={e.path} variant="light" size="xs" loading={carregando === e.path} onClick={() => testar(e.path)}>
              {e.rotulo}
            </Button>
          ))}
        </SimpleGrid>
      </Paper>

      {resultado && (
        <Paper withBorder p="sm" mt="md">
          <Group gap="xs" mb={6}>
            <Badge color={resultado.ok ? 'green' : 'red'}>{resultado.status || 'ERRO'}</Badge>
            <Code>GET {resultado.path}</Code>
          </Group>
          <ScrollArea.Autosize mah={260}>
            <Code block style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
              {JSON.stringify(resultado.data, null, 2)}
            </Code>
          </ScrollArea.Autosize>
        </Paper>
      )}
    </>
  );
}
