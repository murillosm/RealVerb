# RealVerb

RealVerb é um aplicativo Android inovador, desenvolvido como um Trabalho de Conclusão de Curso, que transforma o ambiente ao seu redor numa sala de aula interativa. Utilizando a câmara do seu smartphone, o aplicativo identifica objetos do mundo real e gera frases contextuais para ajudar na aprendizagem de novos idiomas de forma prática e envolvente.

## ✨ Funcionalidades Principais

* **Reconhecimento de Objetos:** Aponte a câmara, tire uma foto e deixe que a Inteligência Artificial identifique objetos ao seu redor.
* **Geração de Frases Contextuais:** Para cada objeto reconhecido, o aplicativo gera duas frases em inglês, mostrando como a palavra é usada em situações reais.
* **Tradução Instantânea:** Com um toque, traduza as frases geradas para o português para garantir a compreensão.
* **Síntese de Voz (Text-to-Speech):** Ouça a pronúncia correta de cada frase em inglês para aprimorar as suas habilidades de *listening* e pronúncia.
* **Gamificação:** Ganhe pontos por cada acerto, perca vidas a cada tentativa errada e compita com outros utilizadores no ranking global para se manter motivado.
* **Glossário Pessoal:** Todas as palavras que aprende são guardadas num glossário visual, com as imagens e frases correspondentes, para que possa rever o seu progresso a qualquer momento.
* **Autenticação Segura:** Crie a sua conta e faça login de forma segura para guardar o seu progresso e pontuação.

## 🛠️ Tecnologias Utilizadas

Este projeto foi construído com tecnologias modernas para garantir uma experiência de utilizador fluida e inteligente:

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **Interface de Utilizador:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Reconhecimento de Objetos:** [TensorFlow Lite](https://www.tensorflow.org/lite) com o modelo pré-treinado **EfficientDet-Lite0**.
* **Geração de Frases:** [Google Gemini 1.5 Flash API](https://ai.google.dev/)
* **Tradução On-Device:** [Google ML Kit Translate](https://developers.google.com/ml-kit/language/translation)
* **Backend e Base de Dados:** [Firebase Authentication](https://firebase.google.com/docs/auth) e [Cloud Firestore](https://firebase.google.com/docs/firestore)
* **Arquitetura:** MVVM (Model-View-ViewModel) com Coroutines para gestão de tarefas assíncronas.

## 🚀 Instalação e Execução

### Opção 1: Instalar o APK (Recomendado para teste rápido)

Pode instalar o aplicativo diretamente no seu dispositivo Android através do ficheiro APK.

1.  **[Faça o download do APK aqui](LINK_PARA_O_SEU_APK_NO_GITHUB_RELEASES)**.
2.  Transfira o ficheiro `.apk` para o seu dispositivo Android.
3.  Abra o gestor de ficheiros, localize o APK e toque nele para instalar. Pode ser necesssário permitir a instalação de fontes desconhecidas nas configurações do seu dispositivo.

### Opção 2: Compilar o Projeto

Para compilar o projeto a partir do código fonte, siga estes passos:

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/seu-usuario/realverb.git](https://github.com/seu-usuario/realverb.git)
    cd realverb
    ```

2.  **Configure o Firebase:**
    * Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
    * Adicione um aplicativo Android ao seu projeto Firebase com o nome de pacote `com.example.projetotcc`.
    * Faça o download do ficheiro `google-services.json` e coloque-o na pasta `app/`.

3.  **Configure a API Key do Gemini:**
    * Crie uma API Key na [Google AI Studio](https://aistudio.google.com/app/apikey).
    * No diretório raiz do projeto, crie um ficheiro chamado `local.properties`.
    * Adicione a sua chave a este ficheiro da seguinte forma:
        ```properties
        apiKey="SUA_API_KEY_AQUI"
        ```

4.  **Construa o projeto:**
    * Abra o projeto no Android Studio e aguarde a sincronização do Gradle.
    * Construa e execute o aplicativo no seu emulador ou dispositivo físico.



---
*Este projeto foi desenvolvido como parte de um Trabalho de Conclusão de Curso, explorando o uso de Inteligência Artificial para criar ferramentas educacionais inovadoras.*
