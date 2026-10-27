// Descreve cada recurso gerenciado pelo ADMIN: rota, colunas da tabela e campos do formulário.
// tipo do campo: texto | email | senha | numero | data | select

const rotuloPessoa = (x) => `${x.nome} (${x.matricula})`;

export const RECURSOS = {
  alunos: {
    titulo: 'Alunos',
    singular: 'aluno',
    base: '/alunos',
    chave: (r) => r.matricula, // PUT/DELETE usam a matrícula
    editavel: true,
    colunas: [
      ['Matrícula', (r) => r.matricula],
      ['Nome', (r) => r.nome],
      ['Email', (r) => r.email],
      ['Curso', (r) => r.curso],
      ['Sem.', (r) => r.semestreDeReferencia],
    ],
    campos: [
      { nome: 'nome', rotulo: 'Nome' },
      { nome: 'email', rotulo: 'Email', tipo: 'email' },
      { nome: 'dataNascimento', rotulo: 'Data de nascimento', tipo: 'data' },
      { nome: 'curso', rotulo: 'Curso' },
      { nome: 'cpfCnpj', rotulo: 'CPF/CNPJ' },
      { nome: 'semestreDeReferencia', rotulo: 'Semestre', tipo: 'numero' },
      { nome: 'telefone', rotulo: 'Telefone' },
      { nome: 'pontuacaoMediaGeral', rotulo: 'Média geral', tipo: 'numero' },
    ],
  },

  professores: {
    titulo: 'Professores',
    singular: 'professor',
    base: '/professor',
    chave: (r) => r.matricula,
    editavel: true,
    colunas: [
      ['Matrícula', (r) => r.matricula],
      ['Nome', (r) => r.nome],
      ['Email', (r) => r.email],
      ['Departamento', (r) => r.departamento],
      ['Titulação', (r) => r.titulacao],
    ],
    campos: [
      { nome: 'nome', rotulo: 'Nome' },
      { nome: 'email', rotulo: 'Email', tipo: 'email' },
      { nome: 'dataNascimento', rotulo: 'Data de nascimento', tipo: 'data' },
      { nome: 'cpfCnpj', rotulo: 'CPF/CNPJ' },
      { nome: 'telefone', rotulo: 'Telefone' },
      { nome: 'departamento', rotulo: 'Departamento' },
      { nome: 'especializacao', rotulo: 'Especialização' },
      { nome: 'titulacao', rotulo: 'Titulação' },
      { nome: 'salario', rotulo: 'Salário', tipo: 'numero' },
      { nome: 'cargaHoraria', rotulo: 'Carga horária (h)', tipo: 'numero' },
      { nome: 'dataAdmissao', rotulo: 'Data de admissão', tipo: 'data' },
    ],
  },

  provas: {
    titulo: 'Provas',
    singular: 'prova',
    feminino: true,
    base: '/provas',
    chave: (r) => r.id,
    editavel: true,
    colunas: [
      ['Id', (r) => r.id],
      ['Disciplina', (r) => r.disciplina],
      ['AV', (r) => r.av],
      ['Nota', (r) => r.nota],
      ['Turma', (r) => r.turma],
      ['Professor', (r) => r.professor?.nome],
      ['Aluno', (r) => r.aluno?.nome],
      ['Aplicação', (r) => r.dataAplicacao],
    ],
    campos: [
      { nome: 'disciplina', rotulo: 'Disciplina' },
      { nome: 'assunto', rotulo: 'Assunto' },
      { nome: 'turma', rotulo: 'Turma' },
      { nome: 'av', rotulo: 'AV (1 a 3)', tipo: 'numero' },
      { nome: 'nota', rotulo: 'Nota', tipo: 'numero' },
      {
        nome: 'professor',
        rotulo: 'Professor',
        tipo: 'select',
        opcoes: (ctx) => ctx.professores.map((p) => ({ value: String(p.id), label: rotuloPessoa(p) })),
        ler: (r) => (r.professor?.id != null ? String(r.professor.id) : ''),
        payload: (v) => ({ id: Number(v) }),
      },
      {
        nome: 'aluno',
        rotulo: 'Aluno',
        tipo: 'select',
        opcoes: (ctx) => ctx.alunos.map((a) => ({ value: String(a.id), label: rotuloPessoa(a) })),
        ler: (r) => (r.aluno?.id != null ? String(r.aluno.id) : ''),
        payload: (v) => ({ id: Number(v) }),
      },
      { nome: 'dataAplicacao', rotulo: 'Data de aplicação', tipo: 'data' },
    ],
  },

  usuarios: {
    titulo: 'Usuários',
    singular: 'usuário',
    base: '/usuarios',
    chave: (r) => r.id,
    editavel: false, // a API não tem edição de conta: apague e recrie
    colunas: [
      ['Id', (r) => r.id],
      ['Nome', (r) => r.nome],
      ['Email', (r) => r.email],
      ['Perfil', (r) => r.papel],
      ['Matrícula', (r) => r.matricula || '—'],
    ],
    campos: [
      { nome: 'nome', rotulo: 'Nome' },
      { nome: 'email', rotulo: 'Email', tipo: 'email' },
      { nome: 'senha', rotulo: 'Senha (mín. 10 caracteres)', tipo: 'senha' },
      {
        nome: 'papel',
        rotulo: 'Perfil',
        tipo: 'select',
        opcoes: () => ['ADMIN', 'PROFESSOR', 'ALUNO'].map((p) => ({ value: p, label: p })),
        aoMudar: () => ({ matricula: '' }), // trocar o perfil limpa a matrícula escolhida
      },
      {
        nome: 'matricula',
        rotulo: 'Vincular a (aluno ou professor)',
        tipo: 'select',
        visivel: (v) => v.papel === 'ALUNO' || v.papel === 'PROFESSOR',
        opcoes: (ctx, v) =>
          (v.papel === 'ALUNO' ? ctx.alunos : ctx.professores).map((x) => ({
            value: x.matricula,
            label: rotuloPessoa(x),
          })),
      },
    ],
  },
};
