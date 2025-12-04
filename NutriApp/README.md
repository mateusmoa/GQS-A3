# NutriApp

Sistema de gestão e cálculo de informação nutricional (RDC nº 429/2020). Projeto full‑stack com backend em Spring Boot e frontend em React (Vite + Tailwind).

## Principais características

- Conformidade com RDC nº 429/2020 (ANVISA)
- Base TBCA (importável via SQL)
- Cálculos automáticos de valores nutricionais e %VD
- Exportação (QR Code, PDF, Excel)
- API REST com endpoints para ingredientes, receitas e cálculos

## Conteúdo do repositório
# NutriApp

Aplicação full‑stack para gestão de ingredientes e cálculo de informação nutricional.

Conteúdo mínimo necessário para rodar a aplicação localmente.

## Pré‑requisitos
- Docker e Docker Compose
- (Opcional para desenvolvimento) Node.js 18+ e Java 17

## Execução (modo recomendado: Docker)

1) Subir os serviços:

```powershell
cd <repo-root>
docker compose -f docker/docker-compose.yml up -d
```

2) Acessos:

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Swagger: http://localhost:8080/swagger-ui.html

## Executar testes

- Backend (maven):

```powershell
cd backend
mvn clean test
```

- Frontend (vitest):

```powershell
cd frontend
npm install
npm run test
```

## Observações importantes
- O banco MySQL inicializa os scripts em `database/` apenas na criação do volume. Para reexecutar a inicialização, remova o volume do MySQL antes de subir os serviços (atenção: destrói dados).
- Dentro do ambiente Docker o frontend deve apontar para `http://nutriapp-backend:8080/api`; ao rodar localmente use `http://localhost:8080/api`.

## Organização dos Dockerfiles

- Os Dockerfiles de build e deploy foram centralizados na pasta `docker/` para facilitar a orquestração e manter as imagens de produção consistentes.
- O `docker/docker-compose.yml` referencia os Dockerfiles com `build.context` apontando para os diretórios de código (`../backend` e `../frontend`) e `dockerfile` apontando para `./Dockerfile.backend` e `./Dockerfile.frontend` dentro de `docker/`.
- Para builds locais por serviço use os comandos de desenvolvimento (ex.: `mvn spring-boot:run` para o backend e `npm run dev` para o frontend). Mantivemos uma única fonte para imagens Docker a fim de evitar duplicação e inconsistências.


## Suporte / Logs
- Logs do backend: `docker logs nutriapp-backend -f`
- Logs do frontend: `docker logs nutriapp-frontend -f`

Arquivo de referência: `docker/docker-compose.yml`

---

Arquivo modificado em: 30/11/2025
