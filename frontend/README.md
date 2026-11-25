# Frontend AutoDiagIA

Interface React + TypeScript + Vite para o sistema de diagnóstico veicular
AutoDiagIA.

## Executar localmente

Na pasta `frontend`:

```powershell
npm install
Copy-Item .env.example .env.local
npm run dev
```

Por padrão, `/api` é encaminhado pelo Vite para `http://localhost:8080`. Defina
`VITE_API_PROXY_TARGET` em `.env.local` se o backend Spring Boot estiver em outro
endereço. `VITE_API_BASE_URL` permite configurar outra URL base da API; uma URL
cross-origin direta exige que o backend ou o proxy de implantação permita essa
origem.

## Verificações

```powershell
npm run build
npm run lint
```

## Telemetria ao vivo

Ao iniciar a atualização ao vivo, a interface consulta repetidamente o endpoint
de leitura dos PIDs selecionados, sem sobrepor requisições. O intervalo mínimo
é 1000 ms, mas a frequência real também depende do tempo de resposta do
adaptador OBD-II. A atualização é interrompida ao sair da página ou ao clicar
em “Parar atualização”.

Durante a atualização, a tela mantém até 60 segundos de amostras numéricas em
memória e exibe uma linha de tendência por leitura. O histórico é temporário:
não é salvo e desaparece ao recarregar ou sair da página.

O endpoint de coleta periódica do backend não é usado nesta tela, pois não
disponibiliza as amostras coletadas à interface.

## Visão geral do veículo

A seção “Visão geral” apresenta um modelo 3D genérico que pode ser girado com o
mouse e possui carroceria translúcida para mostrar componentes como sondas
lambda, catalisadores, sensores e ECU. A seleção de uma peça ou da lista
acessível permite consultar PIDs relacionados pelo endpoint
`GET /telemetry/pid?name={codigo}`.
Resultados e erros permanecem apenas no estado da página; essa consulta não
cria sessões nem persiste leituras. Os 46 PIDs do catálogo têm posições
associadas a componentes ou sistemas do veículo.

O catálogo de PIDs selecionáveis espelha o enum `ObdPid` do backend e deve ser
atualizado quando esse enum mudar.

## Modelos de sessão

A tela de criação oferece sugestões por objetivo e permite salvar modelos
personalizados. Os modelos simples aplicam objetivo e PIDs; os completos também
salvam intervalo, duração máxima e, opcionalmente, o trigger. Modelos
personalizados são compartilhados por todos os usuários da instalação, pois o
projeto ainda não possui autenticação, e ficam no banco de dados. O backend usa
`spring.jpa.hibernate.ddl-auto=update` para preservar os registros existentes e
criar as tabelas dos modelos sem recriar o schema ao iniciar.

Sessões podem ser excluídas pelo painel de detalhes no histórico. A exclusão
remove também os PIDs e registros de telemetria associados; sessões em execução
precisam ser encerradas antes.
