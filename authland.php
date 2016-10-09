<?php
	$lat = $_POST["Latitude"];
	$lon = $_POST["Longitude"];

	$array = array(
		"observations" => array(
			"value" => "1",
			"location" => "$lat $lon"
			)
		);
	
	$ch = curl_init();
 	$headers = array("IDENTITY_KEY: a1bacc80af4aeaa2cff3609bf7139994076b49155a5b3965b8edee46f6d0ee96"); 
	curl_setopt($ch, CURLOPT_URL, "http://api.sentilo.cloud/data/authland/gps-auto/");
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $array); 
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); 

	$output = curl_exec($ch);
	curl_close($ch);

	echo $output;
?>
