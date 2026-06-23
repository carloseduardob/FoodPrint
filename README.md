# FoodPrint 👨‍🍳🥗

O **FoodPrint** é um assistente de cozinha inteligente e gerenciador de inventário de alimentos. O objetivo principal do aplicativo é reduzir o desperdício de alimentos, sugerindo receitas baseadas no que você já tem em casa, guiando o preparo através de um assistente de voz nativo e offline.

## 🚀 Tecnologias Utilizadas

### Core
- **Kotlin**: Linguagem oficial para desenvolvimento Android moderno.
- **Jetpack Compose**: Framework de UI declarativo para criar interfaces dinâmicas e reativas.
- **Material 3**: O mais recente design system do Google para Android.
- **MVVM (Model-View-ViewModel)**: Arquitetura robusta para separação de responsabilidades.
- **Coroutines & Flow**: Gerenciamento de tarefas assíncronas e fluxos de dados em tempo real.

### Bibliotecas & Frameworks
- **DeepL API**: Tradução automática de alta precisão para receitas e ingredientes.
- **Room Database**: Persistência de dados local offline para o inventário e sessões de receitas.
- **Retrofit & Gson**: Consumo de APIs REST para busca de receitas.
- **Coil**: Carregamento de imagens de receitas de forma eficiente e assíncrona.
- **Navigation Compose**: Gerenciamento de rotas e navegação entre telas.
- **CameraX & ML Kit**: Escaneamento de notas fiscais e reconhecimento de texto.
- **Native Voice APIs**:
  - **SpeechRecognizer**: Reconhecimento de voz offline para comandos do chef.
  - **TextToSpeech (TTS)**: Síntese de fala para leitura dos passos da receita.

## 🔌 APIs Utilizadas

- **Spoonacular API**: Motor principal de busca de receitas, fornecendo dados sobre ingredientes, modo de preparo, imagens e valores nutricionais baseados no inventário do usuário.
- **DeepL API**: Serviço de tradução de alta performance utilizado para localizar títulos de receitas, nomes de ingredientes e instruções para o Português (Brasil).
- **ML Kit (Google)**: API de inteligência artificial on-device para reconhecimento de texto (OCR) em notas fiscais e processamento de códigos de barras.

## 📱 Telas do Aplicativo

1. **Dashboard (Início)**: Resumo visual dos itens vencendo hoje e durante a semana, ajudando no planejamento de consumo.
3. **Inventário**: Lista completa de todos os alimentos cadastrados, com controle de validade e quantidade.
4. **Scanner**: Ferramenta para adicionar itens rapidamente ao inventário através da leitura de notas fiscais ou códigos de barras.
5. **Chef (FoodPrint Chef)**:
   - **Busca de Receitas**: Sugere pratos baseados exclusivamente no seu inventário atual.
   - **Detalhes**: Visualização completa da receita (imagem, ingredientes faltantes e passos) com suporte a tradução automática.
   - **Assistente de Voz**: Modo "Mãos-Livres" onde você controla a receita por voz enquanto cozinha.

## 🎧 Comandos do Assistente de Voz

Para ativar o assistente, use a palavra de ativação **"Chefe"**:
- *"Chefe, qual o primeiro passo?"*
- *"Chefe, próximo passo"* ou *"Chefe, continua"*
- *"Chefe, repete o passo"*
- *"Chefe, quais os ingredientes?"*
- *"Chefe, parar"* ou *"Chefe, silêncio"* (Interrompe a fala atual)

## 🛠️ Como rodar o projeto

1. **Clone o repositório** para sua máquina local.
2. **Abra no Android Studio** (Versão Ladybug 2024.2.1 ou superior recomendada).
3. **Aguarde o Gradle Sync**: O projeto utiliza Version Catalog (`libs.versions.toml`) para gerenciar todas as dependências automaticamente.
4. **Permissões**: Ao abrir o app pela primeira vez, aceite a permissão de **Gravação de Áudio** para que o assistente de voz funcione.
5. **Configuração de Voz**: Certifique-se de que o motor de voz do Google está atualizado no seu dispositivo e que o pacote de idioma **Português (Brasil)** está baixado para uso offline.
6. **Execute**: Clique no botão "Run" e selecione um Emulador (com suporte a Google Play Services) ou um Dispositivo Físico.

## 🔒 Configurações de Segurança
O projeto utiliza um arquivo `.gitignore` robusto que protege:
- Chaves de API e assinaturas (`.jks`, `local.properties`).
- Arquivos de configuração da IDE e artefatos de build.

Link apresentação: https://gamma.app/docs/FoodPrint-ph0dmughkctu2fg