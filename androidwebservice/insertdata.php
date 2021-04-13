<?php
    require "dbcon.php";

    $nhietdo = $_POST['nhietdo'];
    $doam = $_POST['doam'];
    $mota = $_POST['mota'];
    date_default_timezone_set('Asia/Ho_Chi_Minh');  // for other timezones, refer:- https://www.php.net/manual/en/timezones.asia.php
    $d = date("Y-m-d");
    $t = date("H:i:s");
    $query = "INSERT INTO environmental VALUES (NULL, '$nhietdo', '$doam', '$mota', '$d', '$t')";

    if (mysqli_query($connect, $query)){
        // thanh cong
        echo "success";

    }else {
        echo "error";
    }

?>