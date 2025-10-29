// src/main/java/com/developersbeeh/medcontrol/data/repository/MotivationalMessageRepository.kt

package com.developersbeeh.medcontrol.data.repository

import javax.inject.Inject
import javax.inject.Singleton

enum class ProfileCategory {
    GENERAL,
    CHILD,
    SENIOR
}

@Singleton
class MotivationalMessageRepository @Inject constructor() {

    private val messages = mapOf(
        ProfileCategory.GENERAL to listOf(
            "Um pequeno passo hoje é um grande salto para sua saúde amanhã. Continue assim!",
            "Lembre-se: consistência é a chave para o bem-estar. Você está no caminho certo!",
            "Cada escolha saudável que você faz é um ato de amor-próprio. Parabéns pela dedicação!",
            "Não se esqueça de beber água! Manter-se hidratado é fundamental para sua energia e saúde.",
            "Um bom dia começa com boas escolhas. Que tal começar com um café da manhã nutritivo?"
        ),
        ProfileCategory.CHILD to listOf(
            "Hora da aventura da saúde! Tomar seus remédios te deixa forte como um super-herói!",
            "Beber água é super divertido e te ajuda a correr e brincar o dia todo!",
            "Não se esqueça das frutinhas! Elas são docinhas e cheias de vitaminas para te dar energia.",
            "Lembre seu cuidador de marcar suas doses. Cada uma é um ponto na sua jornada de campeão!",
            "Uma boa noite de sono ajuda a crescer forte e inteligente. Bons sonhos!"
        ),
        ProfileCategory.SENIOR to listOf(
            "Cuidar da sua saúde é a maior prova de sabedoria. Continue com o excelente trabalho!",
            "Um passo de cada vez. Uma caminhada leve hoje pode trazer grandes benefícios para o seu coração.",
            "Lembre-se de tomar seus medicamentos nos horários certos. Sua saúde agradece por esse cuidado.",
            "Manter-se hidratado é essencial para o bom funcionamento do corpo. Um copo d'água agora pode fazer a diferença.",
            "Sua experiência é valiosa, e sua saúde também. Parabéns por se cuidar com tanto carinho."
        )
    )

    fun getRandomMessageForProfile(category: ProfileCategory): String {
        return messages[category]?.random() ?: messages[ProfileCategory.GENERAL]!!.random()
    }
}