<?php
class TileList
{
    private $result;
	/*
	*/
    public function __construct()
    {
    }

    public function addTile($x,$y,$z)
    {
        $result=pg_query("SELECT * FROM tilelist WHERE x=$x AND y=$y AND z=$z");
        if(pg_num_rows($result) == 0)
        {
            pg_query ("INSERT INTO tilelist(x,y,z) VALUES ($x,$y,$z)");
        }
		/*
		*/
    }

    public function query()
    {
        $this->result=pg_query ("SELECT * FROM tilelist");
    }

    public function nextRow()
    {
        return pg_fetch_array($this->result, null, PGSQL_ASSOC);
    }

    public function deleteTileList()
    {
        pg_query("DELETE FROM tilelist");
    }
}
?>
