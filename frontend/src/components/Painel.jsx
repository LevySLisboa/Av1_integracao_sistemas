import { useState } from 'react';
import { Badge, Button, Group, Stack, Tabs, Text, Title } from '@mantine/core';
import { api } from '../api.js';
import Gerenciar from './admin/Gerenciar.jsx';
import TesteEndpoints from './TesteEndpoints.jsx';

const COR_PAPEL = { ADMIN: 'grape', PROFESSOR: 'blue', ALUNO: 'teal' };

export default function Painel({ usuario, onSair }) {
  const [saindo, setSaindo] = useState(false);

  async function sair() {
    setSaindo(true);
    try {
      await api.logout();
    } finally {
      onSair();
    }
  }

  return (
    <Stack gap="md">
      <Group justify="space-between" align="flex-start">
        <div>
          <Title order={3}>Olá, {usuario.nome}</Title>
          <Text size="sm" c="dimmed">
            {usuario.email}
            {usuario.matricula ? ` · matrícula ${usuario.matricula}` : ''}
          </Text>
        </div>
        <Badge color={COR_PAPEL[usuario.papel] || 'gray'} size="lg" variant="light">
          {usuario.papel}
        </Badge>
      </Group>

      {usuario.papel === 'ADMIN' ? (
        <Tabs defaultValue="gerenciar">
          <Tabs.List>
            <Tabs.Tab value="gerenciar">Gerenciar</Tabs.Tab>
            <Tabs.Tab value="testar">Testar endpoints</Tabs.Tab>
          </Tabs.List>
          <Tabs.Panel value="gerenciar" pt="md">
            <Gerenciar />
          </Tabs.Panel>
          <Tabs.Panel value="testar" pt="md">
            <TesteEndpoints />
          </Tabs.Panel>
        </Tabs>
      ) : (
        <TesteEndpoints />
      )}

      <Button variant="default" onClick={sair} loading={saindo}>
        Sair
      </Button>
    </Stack>
  );
}
