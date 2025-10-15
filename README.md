# Desafio Técnico Backend - Gerenciador de Estacionamento 

Este projeto consiste na construção de um sistema simples para gerenciar um estacionamento, controlando vagas, fluxo de veículos e calculando a receita gerada.

## 📜 Funcionalidades

- **Inicialização Automática:** Ao iniciar, a aplicação consome a API de um simulador para carregar e persistir a configuração da garagem (setores e vagas).
- **Processamento de Eventos via Webhook:** Um endpoint `POST /webhook` recebe e processa eventos em tempo real para:
    - **Entrada (`ENTRY`):** Valida a entrada, aloca uma vaga e aplica regras de preço dinâmico.
    - **Estacionado (`PARKED`):** Valida e corrige a alocação da vaga com base na localização física do veículo.
    - **Saída (`EXIT`):** Libera a vaga, calcula o valor a ser pago com base no tempo de permanência e registra a receita.
- **Regras de Negócio Complexas:**
    - Controle de lotação para múltiplos setores.
    - Preço dinâmico com 4 faixas de ocupação (descontos e acréscimos).
    - Regra de gratuidade para os primeiros 30 minutos.
    - Cálculo de valor por hora cheia (com arredondamento para cima).
- **API de Consulta:** Um endpoint `GET /revenue` para consultar o faturamento total de um setor em uma data específica.
- **Robustez e Validação:** O sistema possui validações para prevenir dados inconsistentes, como entradas duplicadas, timestamps inválidos e alocação em vagas já ocupadas.

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.5.6
- **Persistência:** Spring Data JPA / Hibernate
- **Banco de Dados:** MySQL 8
- **Build Tool:** Maven
- **Containerização:** Docker & Docker Compose
- **Bibliotecas Auxiliares:**
    - Lombok (para redução de código boilerplate)
    - MapStruct (para mapeamento de objetos DTO/Entidade)
- **Testes:** JUnit 5 & Mockito

## 🏛️ Arquitetura

O projeto segue uma arquitetura em camadas (Layered Architecture) para garantir a separação de responsabilidades e a manutenibilidade do código:

- **`Controller`**: Camada responsável por expor os endpoints REST, receber as requisições HTTP e retornar as respostas. Ela delega toda a lógica de negócio para a camada de Serviço.
- **`Service`**: O coração da aplicação. Contém todas as regras de negócio, orquestrando as operações entre os repositórios e mappers.
- **`Repository`**: Camada de acesso a dados, utilizando Spring Data JPA para abstrair as interações com o banco de dados.
- **`Mapper`**: Utiliza MapStruct para converter objetos de forma limpa e automatizada entre as camadas (ex: DTOs para Entidades).
- **`DTO`**: Objetos para transferência de dados, garantindo que a API não exponha diretamente as entidades do banco.
- **`Model/Entities`**: Representação das tabelas do banco de dados como objetos Java.

Fluxo simplificado:
`[Cliente API] -> [Controller] -> [Service] -> [Mapper/Repository] -> [Banco de Dados]`

## 🚀 Como Executar o Projeto

Siga os passos abaixo para ter o ambiente completo rodando localmente.

### Pré-requisitos
- JDK 21 (ou superior)
- Apache Maven 3.8+
- Docker e Docker Compose

### Passos

1.  **Clone o repositório:**
    ```bash
    git clone git@github.com:joaoppegoraro/garage-management.git
    cd garage-management
    ```

2.  **Inicie o Ambiente (Banco de Dados e Simulador):**
    Na raiz do projeto, execute o comando do Docker Compose. Ele irá baixar as imagens do MySQL e do simulador e iniciá-los.
    ```bash
    docker-compose up -d
    ```

3.  **Execute a Aplicação Spring Boot:**
    Você pode executar a aplicação de duas formas:
    - **Via IDE (IntelliJ):** Abra o projeto e execute a classe principal `GarageManagementApplication.java`.
    - **Via Maven (Terminal):** Na raiz do projeto, execute o comando:
      ```bash
      ./mvnw spring-boot:run
      ```

4.  **Verificação:**
    Após alguns segundos, todo o ambiente estará no ar:
    - **Sua Aplicação:** `http://localhost:3003`
    - **Simulador:** `http://localhost:3000`
    - **Banco de Dados MySQL:** `localhost:3306`
