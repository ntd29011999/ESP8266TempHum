<?php
    require "dbcon.php";


    $query = "SELECT * FROM environmental";
    $data = mysqli_query($connect, $query);
    

    //du lieu
class moitruong{
    function moitruong ($id,$nhietdo, $doam, $mota){
        $this->id = $id;
        $this->nhietdo = $nhietdo;
        $this->doam = $doam;
        $this->mota = $mota;
    }
}
// tao mang
$mangmoitruong = array();
// them mang 
while($row = mysqli_fetch_assoc($data)){
    array_push($mangmoitruong, new moitruong($row['id'],$row['nhietdo'],$row['doam'],$row['mota']));
}
echo json_encode($mangmoitruong);
?>