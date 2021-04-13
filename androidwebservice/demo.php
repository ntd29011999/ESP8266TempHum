<?php
//du lieu
class moitruong{
    function moitruong ($nhietdo, $doam){
        $this->Temp = $nhietdo;
        $this->Hum = $doam;
    }
}
// tao mang
$mangmoitruong = array();
// them mang 
array_push($mangmoitruong, new moitruong(25.0, 45.2));
array_push($mangmoitruong, new moitruong(25.5, 45.0));
echo json_encode($mangmoitruong);
?>