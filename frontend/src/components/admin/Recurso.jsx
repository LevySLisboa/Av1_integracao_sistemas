import { useState } from 'react';
import {
  Alert, Button, Group, Modal, PasswordInput, Select, Stack, Table, Text, TextInput,
} from '@mantine/core';
import { api } from '../../api.js';
import { deApi, paraApi } from '../../utils/datas.js';
import ErroAlert from '../ErroAlert.jsx';

function valoresIniciais(cfg, registro) {
  const v = {};
  for (const c of cfg.campos) {
    if (!registro) v[c.nome] = '';
    else if (c.ler) v[c.nome] = c.ler(registro);
    else if (c.tipo === 'data') v[c.nome] = deApi(registro[c.nome]);
    else v[c.nome] = registro[c.nome] == null ? '' : String(registro[c.nome]);
  }
  return v;
}

function montarPayload(cfg, valores) {
  const corpo = {};
  for (const c of cfg.campos) {
    if (c.visivel && !c.visivel(valores)) continue;
    let v = valores[c.nome];
    if (c.payload) v = c.payload(v);
    else if (c.tipo === 'numero') v = v === '' ? null : Number(v);
    else if (c.tipo === 'data') v = paraApi(v);
    corpo[c.nome] = v;
  }
  return corpo;
}

function Campo({ c, valores, contexto, onChange }) {
  const valor = valores[c.nome] ?? '';
  const base = { label: c.rotulo, value: valor, required: true };
  if (c.tipo === 'select') {
    return (
      <Select
        label={c.rotulo}
        required
        searchable
        data={c.opcoes(contexto, valores)}
        value={valor || null}
        onChange={(v) => onChange(c.nome, v ?? '')}
        nothingFoundMessage="Nada encontrado"
      />
    );
  }
  if (c.tipo === 'senha') {
    return (
      <PasswordInput {...base} autoComplete="new-password" onChange={(e) => onChange(c.nome, e.currentTarget.value)} />
    );
  }
  const tipos = { email: 'email', numero: 'number', data: 'date' };
  return (
    <TextInput
      {...base}
      type={tipos[c.tipo] || 'text'}
      step={c.tipo === 'numero' ? 'any' : undefined}
      onChange={(e) => onChange(c.nome, e.currentTarget.value)}
    />
  );
}

export default function Recurso({ cfg, lista, contexto, onMudou }) {
  const fem = !!cfg.feminino;
  const o = fem ? 'a' : 'o';
  const Nome = `${cfg.singular[0].toUpperCase()}${cfg.singular.slice(1)}`;
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [valores, setValores] = useState({});
  const [erro, setErro] = useState(null);
  const [aviso, setAviso] = useState(null);
  const [salvando, setSalvando] = useState(false);

  function abrir(registro) {
    setEditando(registro);
    setValores(valoresIniciais(cfg, registro));
    setErro(null);
    setAberto(true);
  }

  function mudar(nome, valor) {
    const campo = cfg.campos.find((c) => c.nome === nome);
    setValores((atual) => ({ ...atual, [nome]: valor, ...(campo.aoMudar ? campo.aoMudar(valor) : {}) }));
  }

  async function salvar(e) {
    e.preventDefault();
    setErro(null);
    setSalvando(true);
    try {
      const corpo = montarPayload(cfg, valores);
      if (editando) await api.editar(`${cfg.base}/${cfg.chave(editando)}`, corpo);
      else await api.criar(cfg.base, corpo);
      setAberto(false);
      setAviso({ cor: 'green', texto: `${Nome} ${editando ? `atualizad${o}` : `criad${o}`} com sucesso.` });
      await onMudou();
    } catch (err) {
      setErro(err);
    } finally {
      setSalvando(false);
    }
  }

  async function excluir(registro) {
    if (!window.confirm(`Excluir ${fem ? 'esta' : 'este'} ${cfg.singular}?`)) return;
    try {
      await api.remover(`${cfg.base}/${cfg.chave(registro)}`);
      setAviso({ cor: 'green', texto: `${Nome} excluíd${o}.` });
      await onMudou();
    } catch (err) {
      setAviso({ cor: 'red', texto: `Erro ${err.status}: ${err.message}` });
    }
  }

  return (
    <Stack gap="sm">
      <Group justify="space-between">
        <Text size="sm" c="dimmed">
          {lista.length} registro(s)
        </Text>
        <Button size="xs" onClick={() => abrir(null)}>
          {fem ? 'Nova' : 'Novo'} {cfg.singular}
        </Button>
      </Group>

      {aviso && (
        <Alert color={aviso.cor} variant="light" withCloseButton onClose={() => setAviso(null)}>
          {aviso.texto}
        </Alert>
      )}

      <Table.ScrollContainer minWidth={640}>
        <Table striped highlightOnHover verticalSpacing="xs" fz="sm">
          <Table.Thead>
            <Table.Tr>
              {cfg.colunas.map(([rotulo]) => (
                <Table.Th key={rotulo}>{rotulo}</Table.Th>
              ))}
              <Table.Th />
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {lista.length === 0 && (
              <Table.Tr>
                <Table.Td colSpan={cfg.colunas.length + 1}>
                  <Text size="sm" c="dimmed" ta="center">
                    Nenhum registro.
                  </Text>
                </Table.Td>
              </Table.Tr>
            )}
            {lista.map((r) => (
              <Table.Tr key={cfg.chave(r)}>
                {cfg.colunas.map(([rotulo, fn]) => (
                  <Table.Td key={rotulo}>{fn(r)}</Table.Td>
                ))}
                <Table.Td>
                  <Group gap={4} wrap="nowrap" justify="flex-end">
                    {cfg.editavel && (
                      <Button size="compact-xs" variant="subtle" onClick={() => abrir(r)}>
                        Editar
                      </Button>
                    )}
                    <Button size="compact-xs" variant="subtle" color="red" onClick={() => excluir(r)}>
                      Excluir
                    </Button>
                  </Group>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Table.ScrollContainer>

      <Modal
        opened={aberto}
        onClose={() => setAberto(false)}
        title={`${editando ? 'Editar' : fem ? 'Nova' : 'Novo'} ${cfg.singular}`}
        centered
      >
        <form onSubmit={salvar}>
          <Stack gap="sm">
            {cfg.campos
              .filter((c) => !c.visivel || c.visivel(valores))
              .map((c) => (
                <Campo key={c.nome} c={c} valores={valores} contexto={contexto} onChange={mudar} />
              ))}
            <ErroAlert erro={erro} />
            <Button type="submit" loading={salvando}>
              {editando ? 'Salvar alterações' : 'Criar'}
            </Button>
          </Stack>
        </form>
      </Modal>
    </Stack>
  );
}
