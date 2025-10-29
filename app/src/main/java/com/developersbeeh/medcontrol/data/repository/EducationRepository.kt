package com.developersbeeh.medcontrol.data.repository

import com.developersbeeh.medcontrol.data.model.ArtigoEducativo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationRepository @Inject constructor() {

    // Por enquanto, os artigos são fixos. No futuro, podem vir de um CMS ou Firestore.
    fun getArtigos(): List<ArtigoEducativo> {
        return listOf(
            ArtigoEducativo(
                id = "1",
                titulo = "A Importância da Adesão ao Tratamento",
                subtitulo = "Entenda por que seguir a prescrição médica é crucial para sua saúde.",
                categoria = "Medicamentos",
                imageUrl = "https://images.pexels.com/photos/40568/medical-appointment-doctor-healthcare-40568.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    A adesão ao tratamento medicamentoso é um dos pilares para o sucesso terapêutico. Seguir corretamente as orientações de dosagem e horários prescritos pelo seu médico não é apenas uma formalidade, mas uma etapa essencial para garantir que o medicamento atinja o efeito desejado e que sua condição de saúde seja controlada de forma eficaz.

                    **Por que é tão importante?**

                    1. **Eficácia do Medicamento:** Muitos medicamentos precisam manter uma concentração constante no sangue para funcionar corretamente. Pular doses ou tomá-las em horários irregulares pode fazer com que essa concentração caia abaixo do nível terapêutico, tornando o tratamento ineficaz.

                    2. **Prevenção de Complicações:** Em doenças crônicas como hipertensão, diabetes ou colesterol alto, a adesão rigorosa previne complicações graves a longo prazo, como infartos, AVCs e insuficiência renal.

                    3. **Evitar Resistência:** No caso de antibióticos, a interrupção precoce do tratamento pode permitir que as bactérias mais resistentes sobrevivam, levando a infecções mais difíceis de tratar no futuro.

                    O NidusCare foi projetado para ser seu maior aliado nessa jornada. Use os lembretes, acompanhe seu histórico e mantenha seu tratamento sempre em dia!
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "2",
                titulo = "Hidratação: O Combustível Silencioso do Corpo",
                subtitulo = "Descubra os benefícios de beber água na quantidade certa todos os dias.",
                categoria = "Bem-estar",
                imageUrl = "https://images.pexels.com/photos/2294353/pexels-photo-2294353.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    Muitas vezes subestimada, a hidratação adequada é fundamental para quase todas as funções do nosso corpo. A água não apenas mata a sede, mas também regula a temperatura corporal, lubrifica as articulações, auxilia na absorção de nutrientes e elimina toxinas.

                    **Quantos litros devo beber?**

                    A recomendação geral é de cerca de 2 litros por dia, mas essa quantidade pode variar de acordo com seu peso, idade, nível de atividade física e o clima onde você vive. Uma boa dica é observar a cor da sua urina: se estiver clara, você provavelmente está bem hidratado.

                    **Dicas para Beber Mais Água:**

                    * **Tenha uma garrafa sempre por perto:** Mantê-la visível serve como um lembrete constante.
                    * **Use aplicativos de lembrete:** O NidusCare pode te ajudar com lembretes programados para beber água.
                    * **Saborize sua água:** Adicionar rodelas de limão, laranja, folhas de hortelã ou gengibre pode tornar o consumo mais prazeroso.

                    Lembre-se: manter-se hidratado é um dos investimentos mais simples e eficazes que você pode fazer pela sua saúde hoje e no futuro.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "3",
                titulo = "Entendendo a Pressão Arterial",
                subtitulo = "Saiba o que significam os números e como manter sua pressão sob controle.",
                categoria = "Saúde",
                imageUrl = "https://images.pexels.com/photos/4386466/pexels-photo-4386466.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    Aferir a pressão arterial é um procedimento comum em consultas médicas, mas você sabe o que os números "12 por 8" realmente significam? A pressão arterial é a força que o sangue exerce contra as paredes das artérias. Ela é medida em dois números:

                    * **Pressão Sistólica (o número maior):** Mede a pressão nas artérias quando o coração bate.
                    * **Pressão Diastólica (o número menor):** Mede a pressão nas artérias quando o coração está em repouso, entre os batimentos.

                    **Níveis de Pressão:**
                    - **Normal:** Menor que 120/80 mmHg
                    - **Elevada:** Sistólica entre 120-129 e diastólica menor que 80 mmHg.
                    - **Hipertensão (Pressão Alta) Estágio 1:** Sistólica entre 130-139 ou diastólica entre 80-89 mmHg.
                    - **Hipertensão Estágio 2:** Sistólica de 140 mmHg ou mais, ou diastólica de 90 mmHg ou mais.

                    Manter a pressão arterial em níveis saudáveis é vital para prevenir doenças cardíacas, derrames e problemas renais. Um estilo de vida saudável, com dieta balanceada, exercícios regulares e baixo consumo de sódio, é a chave para o controle. Utilize a seção de "Anotações" do NidusCare para registrar suas medições e compartilhar com seu médico.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "4",
                titulo = "Dicas Essenciais para um Envelhecimento Saudável",
                subtitulo = "Como manter a qualidade de vida e a independência na terceira idade.",
                categoria = "Idosos",
                imageUrl = "https://images.pexels.com/photos/3831847/pexels-photo-3831847.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    Envelhecer com saúde é um objetivo comum, e algumas práticas diárias podem fazer toda a diferença para manter a vitalidade, a mente ativa e o corpo forte. Cuidar da saúde na terceira idade envolve uma abordagem holística que vai além da medicação.

                    **Pilares para um Envelhecimento Ativo:**

                    1. **Atividade Física Regular:** Exercícios de baixo impacto como caminhada, natação ou ioga são excelentes para manter a flexibilidade, o equilíbrio e a força muscular, ajudando a prevenir quedas. A OMS recomenda pelo menos 150 minutos de atividade moderada por semana.

                    2. **Alimentação Nutritiva:** Uma dieta rica em frutas, vegetais, grãos integrais e proteínas magras é fundamental. É importante garantir a ingestão adequada de cálcio e vitamina D para a saúde dos ossos.

                    3. **Mente Ativa:** Desafie seu cérebro com leituras, palavras-cruzadas, jogos de tabuleiro ou aprendendo uma nova habilidade. A socialização também é crucial para a saúde mental. Mantenha contato com amigos e familiares.

                    4. **Prevenção é a Chave:** Realize check-ups médicos regulares, mesmo que se sinta bem. Monitore a pressão arterial, a glicemia e o colesterol. Mantenha a carteira de vacinação atualizada, pois vacinas como a da gripe e a pneumocócica são especialmente importantes para idosos.

                    Use o NidusCare para agendar seus exames, registrar suas medições e manter seus medicamentos em dia, garantindo uma vida longa e com qualidade.
                """.trimIndent()
            ),
            // ✅ NOVOS ARTIGOS ADICIONADOS
            ArtigoEducativo(
                id = "5",
                titulo = "Diabetes: Mitos e Verdades",
                subtitulo = "Controle a condição com informação de qualidade e um estilo de vida saudável.",
                categoria = "Saúde",
                imageUrl = "https://images.pexels.com/photos/6942111/pexels-photo-6942111.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    O diabetes é uma condição crônica que afeta milhões de pessoas, mas ainda é cercada de mitos. Entender a verdade é o primeiro passo para um controle eficaz.

                    **Verdade: Dieta é fundamental, mas não se trata apenas de cortar açúcar.**
                    Uma dieta para diabéticos deve ser balanceada, rica em nutrientes, fibras e gorduras saudáveis. O foco é controlar a quantidade e o tipo de carboidratos, não apenas o açúcar.

                    **Mito: "Se tenho diabetes, nunca mais poderei comer doces."**
                    Com moderação e planejamento, uma pessoa com diabetes pode comer doces ocasionalmente. O segredo é contabilizar os carboidratos na contagem total do dia e monitorar a glicemia.

                    **Verdade: Atividade física é um poderoso aliado.**
                    Exercícios ajudam o corpo a usar a insulina de forma mais eficiente, controlando os níveis de açúcar no sangue. Converse com seu médico sobre o melhor tipo de atividade para você.

                    O NidusCare te ajuda a monitorar a glicemia na seção "Anotações", permitindo que você e seu médico entendam como sua dieta e rotina impactam sua saúde.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "6",
                titulo = "O Poder do Sono Reparador",
                subtitulo = "Por que dormir bem é tão vital para a sua saúde física e mental.",
                categoria = "Bem-estar",
                imageUrl = "https://images.pexels.com/photos/3771089/pexels-photo-3771089.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    O sono não é um luxo, mas uma necessidade biológica fundamental. Durante o sono, nosso corpo trabalha para reparar músculos, consolidar memórias e liberar hormônios que regulam o crescimento e o apetite.

                    **Quais os benefícios de uma boa noite de sono?**
                    - Melhora a memória e o aprendizado.
                    - Fortalece o sistema imunológico.
                    - Reduz o estresse e melhora o humor.
                    - Ajuda a manter um peso saudável.
                    - Reduz o risco de doenças crônicas, como diabetes e problemas cardíacos.

                    **Dicas para uma Higiene do Sono Eficaz:**
                    * **Crie uma Rotina:** Tente ir para a cama e acordar nos mesmos horários todos os dias.
                    * **Desconecte-se:** Evite telas de celular, TV ou computador pelo menos uma hora antes de dormir. A luz azul pode atrapalhar a produção de melatonina, o hormônio do sono.
                    * **Crie um Ambiente Confortável:** Mantenha seu quarto escuro, silencioso e com uma temperatura agradável.
                    * **Evite Cafeína e Álcool à Noite:** Essas substâncias podem interferir na qualidade do seu sono.

                    Use a função "Monitoramento de Sono" no NidusCare para registrar seus horários e a qualidade do seu descanso.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "7",
                titulo = "Ansiedade e Estresse: Como Lidar?",
                subtitulo = "Estratégias práticas para acalmar a mente e encontrar o equilíbrio.",
                categoria = "Saúde Mental",
                imageUrl = "https://images.pexels.com/photos/3822622/pexels-photo-3822622.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    Estresse e ansiedade são reações normais aos desafios da vida, mas quando se tornam crônicos, podem afetar gravemente nossa saúde. Aprender a gerenciá-los é essencial para o bem-estar.

                    **Qual a diferença?**
                    O **estresse** é geralmente uma resposta a um fator externo (um prazo no trabalho, uma briga). Ele tende a desaparecer quando o gatilho é removido. A **ansiedade** é uma preocupação persistente e excessiva que não desaparece, mesmo sem um gatilho aparente.

                    **Técnicas para Aliviar o Estresse e a Ansiedade:**

                    * **Respiração Profunda:** Inspire lentamente pelo nariz por 4 segundos, segure por 4 segundos e expire lentamente pela boca por 6 segundos. Repita várias vezes.
                    * **Atenção Plena (Mindfulness):** Concentre-se no presente. Observe seus pensamentos e sentimentos sem julgamento. Preste atenção aos seus 5 sentidos.
                    * **Atividade Física:** A prática regular de exercícios libera endorfinas, que têm um efeito calmante natural.
                    * **Conecte-se com Outros:** Conversar com amigos, familiares ou um profissional pode aliviar o peso das preocupações.

                    **Quando procurar ajuda?**
                    Se a ansiedade ou o estresse estão interferindo em sua vida diária, no trabalho ou nos relacionamentos, não hesite em procurar um psicólogo ou psiquiatra. Cuidar da mente é tão importante quanto cuidar do corpo.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "8",
                titulo = "Nutrição Inteligente: O Prato Ideal",
                subtitulo = "Montar refeições balanceadas é mais fácil do que você imagina.",
                categoria = "Nutrição",
                imageUrl = "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    Uma alimentação saudável não precisa ser complicada ou sem graça. O segredo está no equilíbrio dos nutrientes. Um método simples para visualizar isso é o "Prato Saudável".

                    **Como Montar seu Prato:**

                    Imagine dividir seu prato em quatro partes:

                    1. **Metade do Prato (50%): Vegetais e Folhas.**
                    Preencha metade do seu prato com uma variedade colorida de vegetais, como brócolis, cenoura, tomate, abobrinha e folhas verdes. Eles são ricos em vitaminas, minerais e fibras.

                    2. **Um Quarto do Prato (25%): Proteínas Magras.**
                    Escolha fontes de proteína de alta qualidade, como peixe, frango, ovos, feijão, lentilha ou tofu. A proteína é essencial para a construção e reparo dos tecidos do corpo.

                    3. **Um Quarto do Prato (25%): Carboidratos Integrais.**
                    Opte por grãos integrais como arroz integral, quinoa, aveia e pão integral. Eles fornecem energia de forma mais lenta e sustentada, além de serem ricos em fibras.

                    **Não se esqueça:**
                    * **Gorduras Saudáveis:** Inclua fontes como abacate, nozes, sementes e azeite de oliva.
                    * **Frutas:** Consuma frutas de cores variadas como sobremesa ou nos lanches.
                    * **Hidratação:** Beba bastante água ao longo do dia.

                    Use o "Diário Alimentar" do NidusCare para registrar suas refeições e criar um padrão de alimentação mais saudável.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "9",
                titulo = "Febre em Crianças: O que Fazer?",
                subtitulo = "Um guia rápido para pais e cuidadores sobre como agir quando a temperatura sobe.",
                categoria = "Crianças",
                imageUrl = "https://images.pexels.com/photos/3992870/pexels-photo-3992870.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    A febre é um dos sintomas mais comuns na infância e, embora assuste, geralmente é um sinal de que o corpo está combatendo uma infecção.

                    **Como Agir:**
                    1. **Meça a Temperatura:** Use um termômetro digital para obter uma medição precisa. Considera-se febre temperaturas acima de 37.8°C (axilar).
                    2. **Mantenha a Criança Confortável:** Vista a criança com roupas leves e ofereça bastante líquido (água, sucos, chás) para evitar a desidratação.
                    3. **Medicamentos:** NUNCA medique uma criança sem orientação médica. O pediatra irá indicar o antitérmico correto e a dosagem adequada com base no peso da criança.
                    4. **Banhos:** Banhos mornos (nunca frios ou com álcool) podem ajudar a baixar a temperatura e proporcionar alívio, mas não são um substituto para a medicação, se indicada.

                    **Quando Procurar um Médico com Urgência?**
                    * Bebês com menos de 3 meses com febre.
                    * Febre muito alta (acima de 39.5°C) que não baixa com medicação.
                    * Se a criança estiver muito prostrada, irritada, com dificuldade para respirar, manchas na pele ou apresentar convulsão.
                    * Se a febre persistir por mais de 48-72 horas.

                    Na dúvida, sempre entre em contato com o pediatra. Use o NidusCare para registrar as medições de temperatura e as doses dos medicamentos administrados.
                """.trimIndent()
            ),
            ArtigoEducativo(
                id = "10",
                titulo = "Prevenção: O Melhor Remédio",
                subtitulo = "Atitudes que você pode tomar hoje para uma vida mais longa e saudável.",
                categoria = "Prevenção",
                imageUrl = "https://images.pexels.com/photos/1153369/pexels-photo-1153369.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                conteudo = """
                    Muitas das doenças mais comuns da atualidade, como problemas cardíacos, diabetes tipo 2 e certos tipos de câncer, podem ser prevenidas com hábitos de vida saudáveis. Investir na prevenção é investir no seu futuro.

                    **Pilares da Saúde Preventiva:**

                    * **Alimentação Balanceada:** Como vimos no artigo sobre o prato ideal, uma dieta rica em alimentos naturais e pobre em ultraprocessados é a base de tudo.
                    * **Atividade Física Regular:** A OMS recomenda de 150 a 300 minutos de atividade moderada por semana. Encontre algo que você goste: caminhar, dançar, nadar, correr. O importante é se mover!
                    * **Não Fumar e Moderar o Álcool:** O tabagismo é a principal causa de morte evitável no mundo. O consumo excessivo de álcool também está ligado a diversas doenças.
                    * **Sono de Qualidade:** Dormir de 7 a 9 horas por noite é crucial para a recuperação do corpo e da mente.
                    * **Check-ups Regulares:** Consulte seu médico anualmente para exames de rotina, mesmo que se sinta bem. Diagnosticar problemas precocemente aumenta drasticamente as chances de sucesso no tratamento.
                    * **Vacinação em Dia:** Vacinas não são apenas para crianças. Adultos e idosos também precisam se proteger contra doenças como gripe, pneumonia, tétano e herpes zoster.

                    Use o NidusCare para agendar seus check-ups e manter sua carteira de vacinação organizada!
                """.trimIndent()
            )
        )
    }
}