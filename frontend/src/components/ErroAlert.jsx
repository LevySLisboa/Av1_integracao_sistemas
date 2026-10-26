import { Alert, List, Text } from '@mantine/core';

/** Mostra o erro da API com o status HTTP em destaque (e os campos inválidos, se houver). */
export default function ErroAlert({ erro }) {
  if (!erro) return null;
  const campos = erro.body?.campos ? Object.entries(erro.body.campos) : [];
  return (
    <Alert color="red" variant="light" title={erro.status ? `Erro ${erro.status}` : 'Sem conexão'}>
      <Text size="sm">{erro.message}</Text>
      {campos.length > 0 && (
        <List size="sm" mt={4}>
          {campos.map(([campo, msg]) => (
            <List.Item key={campo}>
              {campo}: {msg}
            </List.Item>
          ))}
        </List>
      )}
    </Alert>
  );
}
