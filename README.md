# 🐦 FlappyBirdIA

> Uma recriação do clássico **Flappy Bird** com um sistema de **Inteligência Artificial** capaz de aprender a jogar sozinho.

---

## 🧠 Visão Geral

O **FlappyBirdIA** é um projeto desenvolvido em **Java** que combina a implementação do jogo **Flappy Bird** com um sistema de **IA baseada em redes neurais** e **algoritmos genéticos**.  
O objetivo é treinar uma população de agentes (pássaros) para aprenderem a jogar sozinhos, ajustando seus pesos neurais a cada geração com base no desempenho (fitness) obtido.

Este projeto foi desenvolvido com fins **educacionais** e **experimentais**, sendo ideal para quem deseja compreender na prática como uma IA pode evoluir e aprender com tentativa e erro.

---

## 🎮 Funcionalidades

- 🕹️ Jogo completo com sprites, colisão e física realista.  
- 🧩 Algoritmo genético para evolução da IA.  
- 🧠 Rede neural feedforward simples para tomada de decisão.  
- 📊 Sistema de pontuação e medição de fitness.  
- 🌤️ Cenário dinâmico com múltiplas camadas (nuvens, chão, tubos, etc).  
- 🔄 Visualização do treinamento em tempo real.  
- 💾 Salvamento e carregamento de gerações de IA.

---

## 🛠️ Tecnologias Utilizadas

- **Java 17+**
- **JavaFX** (renderização do jogo)
- **Maven** (gerenciador de dependências)
- **Programação Orientada a Objetos**
- **Redes Neurais e Algoritmos Genéticos**

---

## 🚀 Como Executar

### 🔧 Pré-requisitos
Antes de iniciar, verifique se você possui:
- [Java JDK 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) instalado
- [Apache Maven](https://maven.apache.org/download.cgi)
- As imagens na pasta `/sprites` conforme referenciadas no código:
/sprites/chao.png
/sprites/nuvem.png
/sprites/predio.png
/sprites/arvore.png
/sprites/cano.png
/sprites/passaro.png

---

### ▶️ Executando o projeto

```bash
# Clone o repositório
git clone https://github.com/cherohn/FlappyBirdIA.git
cd FlappyBirdIA

# Compile o projeto
mvn clean install

# Execute o jogo
mvn exec:java -Dexec.mainClass="main.FlappyBirdGame"
```
---

🧬 Como funciona a IA

A IA é baseada em um sistema de DNA -> Rede Neural -> Ação.
Cada pássaro possui um DNA, que define os pesos da rede neural.
Durante o jogo:

A rede neural recebe entradas como:

Posição vertical do pássaro

Distância até o próximo cano

Altura do espaço entre os canos

O output da rede define se o pássaro pula ou continua voando.

Após a morte de todos os pássaros, um novo conjunto (geração) é criado com base nos melhores indivíduos anteriores.

O fitness de cada pássaro é calculado pela pontuação obtida no jogo.
Os melhores DNAs são cruzados e sofrem pequenas mutações, criando uma nova geração mais adaptada.

---

⚙️ Parâmetros Importantes

Os parâmetros de treino podem ser ajustados diretamente no código:

Parâmetro	Descrição	Padrão

POPULATION_SIZE	Quantidade de pássaros por geração	100

MUTATION_RATE	Probabilidade de mutação no DNA	0.05

INPUTS	Quantidade de variáveis de entrada da rede neural	3

HIDDEN	Neurônios na camada oculta	6

OUTPUTS	Ações possíveis (pular ou não)	1

---

🧪 Testando e Visualizando

Durante a execução, você poderá visualizar o desempenho da população:

Pássaros vivos são renderizados na tela.

A cada geração, o score médio tende a aumentar.

É possível observar a evolução da IA em tempo real.

---

📜 Licença

Este projeto é distribuído sob a licença MIT.
Sinta-se livre para usar, estudar e modificar conforme desejar.

MIT License © 2025 cherohn

---

👤 Autor

Cherohn
https://github.com/cherohn

💬 Projeto desenvolvido com fins educacionais e de pesquisa em Inteligência Artificial.
