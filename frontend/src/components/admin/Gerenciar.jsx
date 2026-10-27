import { useCallback, useEffect, useState } from 'react';
import { Center, Loader, Tabs } from '@mantine/core';
import { api } from '../../api.js';
import ErroAlert from '../ErroAlert.jsx';
import { RECURSOS } from './config.js';
import Recurso from './Recurso.jsx';

export default function Gerenciar() {
  const [dados, setDados] = useState({ alunos: [], professores: [], provas: [], usuarios: [] });
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);

  const carregar = useCallback(async () => {
    try {
      const [alunos, professores, provas, usuarios] = await Promise.all([
        api.listar('/alunos'),
        api.listar('/professor'),
        api.listar('/provas'),
        api.listar('/usuarios'),
      ]);
      setDados({ alunos, professores, provas, usuarios });
      setErro(null);
    } catch (e) {
      setErro(e);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  if (carregando) {
    return (
      <Center py="xl">
        <Loader size="sm" />
      </Center>
    );
  }
  if (erro) return <ErroAlert erro={erro} />;

  return (
    <Tabs defaultValue="alunos" keepMounted={false}>
      <Tabs.List>
        {Object.entries(RECURSOS).map(([chave, cfg]) => (
          <Tabs.Tab key={chave} value={chave}>
            {cfg.titulo}
          </Tabs.Tab>
        ))}
      </Tabs.List>
      {Object.entries(RECURSOS).map(([chave, cfg]) => (
        <Tabs.Panel key={chave} value={chave} pt="md">
          <Recurso cfg={cfg} lista={dados[chave]} contexto={dados} onMudou={carregar} />
        </Tabs.Panel>
      ))}
    </Tabs>
  );
}
