# <p align="center">PROJETO API DE GERENCIAMENTO DE ESTOQUE</p>


Este projeto é uma API de gerenciamento de estoque, com movimentações em lote e permissões baseadas em roles. Foi projetada focando principalmente em pequenos/médios negócios do ramo alimentício.

## Arquitetura

Neste projeto é utilizada a arquitetura em camadas, onde a Repository Layer é responsável pelas operações de banco de dados, a Service Layer concentra a lógica de negócio e a Controller Layer atua como interface REST. Essa abordagem facilita a execução de testes e a realização de manutenções futuras, pois cada camada possui um papel bem definido.

## Tecnologias

- Java 21;
- Spring Boot 3.5.4;
- Spring Security;
- JWT 0.11.5
- Lombok;
- PostgreSQL;
- JUnit e Mockito;
- Swagger OpenAPI.

## Funcionalidades

**Autenticação via Usuário/Senha e geração de token JWT com Spring Security:** A escolha desta forma de autenticação se deu por ser simples para os usuários com a parte de login, mas ainda assim fornecer uma camada de segurança através da geração de um token JWT para validar a sessão. Quando o usuário insere as credenciais corretas, é gerado um token que expirará após um certo período de tempo.

<img width="1370" height="500" alt="Geração token" src="https://github.com/user-attachments/assets/cdfe99f5-9d75-4415-b8bf-476fc60ff702" />

**Proteção de endpoints com Spring Security:** Defini que a autorização será baseada em 3 roles distintas (ADMIN, VOLUNTARIO E FUNCIONARIO) e organizei o de acesso aos endpoints baseados nelas. Também utilizei o @PreAutorize para implementar segurança em métodos em que apenas um usuário com uma role específica pode ter acesso.

<img width="968" height="162" alt="@PreAutorize" src="https://github.com/user-attachments/assets/269718ad-2d99-4a29-aff1-0f1acaa193aa" />

**DTOs de Request/Response:** Adotei o padrão DTO para garantir que nenhuma informação sensível, como por exemplo a senha de um usuário, ficasse acessível para um terceiro. Além disso, é uma boa prática do Spring utilizar DTOs para manipulação de dados ao invés das entidades para garantir uma manutenção mais fácil da aplicação. Para as validações, utilizei a biblioteca Jakarta Validation em conjunto com expressões regulares (regex).

<img width="1088" height="553" alt="Dto" src="https://github.com/user-attachments/assets/d91e6e36-dcb9-448e-bc95-f630e289c5fc" />

**Mappers para conversão de dados:** Juntamente ao padrão DTO implementei classes de mappers, com o objetivo de facilitar a conversão entidade/DTO e vice-versa.

<img width="1034" height="762" alt="Mapper" src="https://github.com/user-attachments/assets/000bd82f-c2a1-417f-878e-60ffd102f89e" />

**Validações personalizadas:** Foram implementadas validações na Service Layer de acordo com regras de negócio específicas. Por exemplo, ao excluir um movimento do tipo USO_PRODUCAO ou PERDA, o sistema não apenas deleta o registro, mas também reverte a baixa feita no lote e atualiza o estoque do produto.

<img width="1215" height="458" alt="validações" src="https://github.com/user-attachments/assets/507d25b5-b7cc-4a11-80cd-592715b026f7" />

**Operações de CRUD:** Criei operações de CRUD para todas as entidades, implementando as regras de negócio pertinentes na Service Layer, como por exemplo a regra de que só um usuário com role ADMIN ou SUPERVISOR possui permissão para excluir um produto inativo.

<img width="963" height="437" alt="crud" src="https://github.com/user-attachments/assets/12b29fa1-7ba5-4ddc-a883-89cfd260e6d3" />

**Scheduler:** Um processo agendado (@Scheduled) verifica periodicamente o estoque total dos produtos.Embora o cálculo principal aconteça em tempo real durante a criação ou reversão de movimentos, o scheduler atua como um mecanismo de segurança para garantir a consistência dos dados.

<img width="1130" height="591" alt="Scheduler" src="https://github.com/user-attachments/assets/3ad67d90-6f05-431f-bfef-076b2cacf97e" />

**Tratamento global de exceções:** O tratamento de exceções está centralizado em um @ControllerAdvice GlobalExceptionHandler, que captura os erros mais comuns e retorna respostas mais amigáveis ao usuário.

<img width="1099" height="792" alt="exception" src="https://github.com/user-attachments/assets/a2db7526-bc9d-4758-807c-ea650190b699" />

**Documentação dos endpoints:** A documentação dos endpoints foi realizada através do Swagger, com a possibilidade de testar as requisições diretamente no navegador e com exemplos já estruturados para maior facilidade de entendimento.

<img width="1861" height="777" alt="Swagger" src="https://github.com/user-attachments/assets/3a5cbd11-8dc3-4962-8560-a3ac96f1b91c" />

## Fluxo de Negócio

```mermaid
flowchart TD
    A[Usuário] -->|Login| B[Autenticação]
    B -->|Credenciais válidas| C[JWT Token]
    B -->|Inválido| X[Erro 401 - Não Autorizado]

    C --> D[Operações no Sistema]

    D -->|Cadastrar Produto| E[Produto]
    D -->|Cadastrar Lote| F[Lote]
    D -->|Registrar Movimento| G[Movimento]

    G -->|USO ou PERDA| H[Atualizar Estoque]
    H -->|Recalcular Quantidade| I[Atualização no Produto]

    style X fill:#f77,stroke:#333,stroke-width:2px
    style C fill:#7f7,stroke:#333,stroke-width:2px
    style H fill:#7af,stroke:#333,stroke-width:2px
