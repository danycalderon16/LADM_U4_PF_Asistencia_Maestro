package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models

class List {
    /************************************
     * DANIEL ALEJANDRO CALDERÓN VIGREN *
     ************************************/

    var id = ""
    var noControl = ""
    var name = ""
    var date = ""
    var hour = ""

    override fun toString(): String {
        return "id $id, nc $noControl, name $name, date $date, hour $hour"
    }
}