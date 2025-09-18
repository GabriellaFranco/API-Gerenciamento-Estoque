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

## Regras de Negócio

**UserService**

Cadastro:

- O email deve ser único (não pode haver outro usuário com o mesmo email);
- A senha é criptografada antes de ser salva no banco;
- O usuário é criado como ativo por padrão (isActive = true);
- O perfil do usuário é traduzido em uma Authority (ex: ADMIN → role "ADMIN").

Update:

- Apenas o usuário logado pode alterar seu próprio email;
- Usuários inativos não podem alterar o email;

Exclusão:

- Apenas usuários com role ADMIN podem excluir;
- Não permite excluir usuários ativos (isActive = true).

**AuthenticationService**

Alteração de senha:

- Apenas o usuário logado pode alterar sua própria senha;
- Usuários inativos não podem alterar a senha;
- A senha atual deve ser informada corretamente no momento da mudança;
- A nova senha não pode ser igual à atual.
- A nova senha é criptografada antes de salvar.

Atualização de perfil e permissões:

- Apenas usuários com role ADMIN podem executar esta ação;
- Não é permitido alterar o perfil e permissões de usuários inativos;

**ProductService**

Crição do produto:

- Nome do produto deve ser único;
- Os produtos são criados como ativos (isActive = true);
- Unidade de medida deve ser compatível com a categoria do produto;

Atualização do produto:

- Só é possível atualizar produtos que estão ativos;

Exclusão de produto:

- Apenas usuários com perfil/role "SUPERVISOR" podem excluir produtos;
- Somente produtos desativados podem ser excluídos.

**LotService**

Criação de lote:

- Só é possível criar um lote se o produto existir previamente no sistema e estiver ativo (isActive = true);
- Também é necessário que o fornecedor esteja cadastrado previamente no sistema e esteja ativo;
- Ao ser criado, o lote recebe um código aleatório de 15 caracteres(letras e números);
- A unidade de medida do lote é herdada do produto;
- A data de entrada é definida como a data atual;
- O status inicial do lote é definido como ATIVO;

Exclusão de lote:

Apenas usuários com role SUPERVISOR podem excluir lotes (@PreAuthorize("hasRole('SUPERVISOR')");
Só é possível excluir lotes com status ESGOTADO ou VENCIDO;
Não é permitido excluir lotes com status ATIVO;

Atualização de status do lote:

- A atualização do status do lote é realizada de forma automática a cada 2 min (@Scheduled(cron = "0 */2 * * * *");
- São verificados apenas os lotes com status ATIVO;
- Se a data de validade já passou, atualiza o status para VENCIDO. Se a quantidade atual do lote for zero, atualiza para ESGOTADO;
- Se o status mudar, o lote é salvo e o estoque total do produto é recalculado;

Atualização do estoque total dos produtos:

- O estoque total de um produto é a soma da quantidade atual de todos os seus lotes com status ATIVO;
- O estoque é recalculado somente quando necessário, não em lote:
	Ao criar um novo lote → recalcula estoque do produto;
	Ao excluir um lote → recalcula estoque do produto;
	Ao mudar o status de um lote para VENCIDO ou ESGOTADO → recalcula estoque do produto;

Consistência de dados:

- Após criar ou excluir um lote, o sistema recalcula o estoque total de todos os produtos ativos;
- Garante que o totalStock do produto reflita o estado real do estoque com base nos lotes e movimentações;

**InventoryMovementService**

Criação de movimentação:

- É possível a criação de movimentações USO_PRODUÇÃO e PERDA;
- Para criar uma movimentação, o lote associado deve existir;
- O usuário logado é automaticamente associado à movimentação;
- A unidade de medida da movimentação é herdada do lote;
- A data e hora da movimentação são definidas no momento da criação usando LocalDateTime.now();
- Após salva a movimentação, o sistema atualiza a quantidade atual do lote e recalcula o estoque total do produto;
- Se o lote zerar (currentQtd == 0) e não estiver vencido, seu status muda para ESGOTADO.

Exclusão de movimentação:

- Apenas usuários com papel SUPERVISOR podem excluir movimentações (@PreAuthorize("HasRole('SUPERVISOR')");
- Ao excluir, a quantidade da movimentação é devolvida ao lote;
- Se o lote estava ESGOTADO e a devolução restaura estoque (currentQtd > 0), o status volta para ATIVO;
- O estoque total do produto é recalculado.
  
## Fluxo de Negócio Simplificado

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
