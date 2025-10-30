NidusCare - Cuidando de Perto

NidusCare é um app nativo Android (Kotlin) para gestão completa de saúde, focado em cuidadores e dependentes. Oferece lembretes de medicação, agenda de saúde, controle de estoque e timeline de atividades. Utiliza IA (Gemini) para escanear receitas e gerar insights, com backend 100% Firebase.
✨ Principais Funcionalidades

    Gestão de Medicamentos: Cadastro completo de posologia, horários, estoque e lembretes com notificações.
    Múltiplos Dependentes: Um único cuidador pode gerenciar a saúde de múltiplos pacientes (dependentes).
    Círculo de Cuidado: Permite que múltiplos cuidadores colaborem no cuidado de um mesmo dependente.
    Farmacinha (Caixa de Remédios): Um inventário para medicamentos de uso esporádico (não contínuo).
    Timeline de Saúde: Um log de eventos unificado com todas as doses, anotações e atividades do dependente.
    Relatórios de Adesão: Gráficos e estatísticas sobre a adesão ao tratamento.
    Busca de Farmácias: Encontra farmácias próximas usando a API do Google Places.
    Central Educativa: Artigos sobre saúde e bem-estar.
    Métricas de Bem-Estar: Monitoramento de hidratação, peso, sono e atividades físicas.
  
🤖 Funcionalidades com IA (Premium)

    Scanner de Receitas (Gemini): Tira uma foto de uma receita médica e cadastra múltiplos medicamentos automaticamente.
    Scanner de Caixa de Remédio (Gemini): Tira uma foto da caixa do remédio para preencher automaticamente o nome, estoque, classe terapêutica e indicações na Farmacinha.
    Insights Proativos (Gemini): O sistema analisa a rotina do paciente e gera insights, como "Percebemos que as doses noturnas são esquecidas com mais frequência".

🚀 Tech Stack

    Linguagem: 100% Kotlin
    Arquitetura: MVVM (Model-View-ViewModel)
    UI: Android XML com Material Design 3
    Navegação: Jetpack Navigation Component (Single Activity)
    Programação Assíncrona: Kotlin Coroutines & Flow
    Injeção de Dependência: Hilt (Dagger)
    Networking: Retrofit2 & OkHttp (para Google Places)
    Backend: Firebase (Auth, Firestore, Storage, Cloud Functions)
    IA (Backend): Google Vertex AI (Gemini 2.5 Pro Vision)
    Bibliotecas de UI: Lottie (Animações), MPAndroidChart (Gráficos), Coil (Imagens)

⚖️ Licença

Este projeto é licenciado sob os termos da Licença Apache 2.0 (arquivo LICENSE).

Em termos simples, isso significa que você tem a liberdade de:

    Usar o software comercialmente.
    Modificar o código-fonte.
    Distribuir suas próprias versões.

Você só precisa:

    Incluir uma cópia da licença.
    Indicar se você fez alterações no código original.
