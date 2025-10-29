// src/main/java/com/developersbeeh/medcontrol/data/model/PermissaoTipo.kt
package com.developersbeeh.medcontrol.data.model

enum class PermissaoTipo(val key: String, val displayName: String, val description: String) {
    REGISTRAR_DOSE("podeRegistrarDose", "Registrar Doses", "Permite que o dependente marque medicamentos como 'tomados'."),
    REGISTRAR_ANOTACOES("podeRegistrarAnotacoes", "Registrar Anotações de Saúde", "Permite adicionar medições como pressão, glicemia, humor, etc."),
    VER_DOCUMENTOS("podeVerDocumentos", "Visualizar Documentos", "Permite o acesso à seção de exames e receitas."),
    ADICIONAR_DOCUMENTOS("podeAdicionarDocumentos", "Adicionar/Editar Documentos", "Permite que o dependente adicione ou edite seus próprios documentos e exames."),
    VER_AGENDA("podeVerAgenda", "Visualizar Agenda", "Permite que o dependente veja os próximos exames e consultas."),

    // ✅ NOVAS PERMISSÕES ADICIONADAS
    ADICIONAR_AGENDAMENTOS("podeAdicionarAgendamentos", "Adicionar Agendamentos", "Permite que o dependente crie novos eventos na agenda."),
    EDITAR_AGENDAMENTOS("podeEditarAgendamentos", "Editar/Excluir Agendamentos", "Permite que o dependente edite ou remova eventos existentes na agenda.")
}