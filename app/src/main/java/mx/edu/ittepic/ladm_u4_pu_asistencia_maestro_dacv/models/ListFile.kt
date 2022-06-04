package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models

class ListFile {

    var id = ""
    var grupo =""
    var dia =""

    override fun toString(): String {
        return "Id: $id, Grupo: $grupo, Dia: $dia"
    }
}