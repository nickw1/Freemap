
// scale is pixels per metre 
function FootpathRenderer(ctx,tile,styles)
{
    this.ctx=ctx;
    this.tile=tile;
    this.styles = styles;
    if(this.ctx.setLineDash === undefined)
    {
        if(this.ctx.mozDash !== undefined)
        {
            this.ctx.setLineDash = (function(pattern)
            {
                this.ctx.mozDash = pattern;
            }).bind(this);
        }
        else
        {
            this.ctx.setLineDash = function(pattern) { }
        }
    };

}

FootpathRenderer.prototype.drawPath = function(f)
{
    this.ctx.beginPath();
	var geom = f.geometry.type=="MultiLineString" ? f.geometry.coordinates: 
		[f.geometry.coordinates];
	for(var i=0; i<geom.length; i++)
	{
    	var p = this.tile.getPoint (geom[i][0]);
    	var style = this.styles.getStyle(f.properties);
		if(style)
		{
    		this.ctx.strokeStyle = style.colour ? style.colour: 'black'; 
    		if(style.dash)
        		this.ctx.setLineDash(style.dash);
    		//status(p.x+" "+ p.y);
    		this.ctx.lineWidth = style.width? style.width: 1;
		}
		else
		{
			this.ctx.strokeStyle = 'black';
			this.ctx.setLineDash([2,2]);
			this.ctx.lineWidth = 1;
		}
    	this.ctx.moveTo(p.x,p.y);
    	for(var j=1; j<geom[i].length; j++)
    	{
        	p = this.tile.getPoint(geom[i][j]);
        	//status(p.x+" "+ p.y);
        	this.ctx.lineTo(p.x,p.y);
    	}
    	this.ctx.stroke();
	}	
}

function Tile(x,y,scale)
{
    this.topLeft = { x:x*1000, y:y*1000 };
    this.scale = scale;
}

Tile.prototype.getPoint = function(coords)
{
    return { x: (coords[0]-this.topLeft.x)*this.scale, 
            y: (this.topLeft.y-coords[1])*this.scale };
}

Tile.prototype.toString = function()
{
    return this.topLeft.x+" "+ this.topLeft.y+" "+ this.scale;
}

Tile.prototype.getBboxString = function()
{
    return "bbox="+this.topLeft.x+","+(this.topLeft.y-1000)+
        ","+(this.topLeft.x+1000)+","+this.topLeft.y;
}

function Styles(style)
{
    this.style = style;
}

Styles.prototype.getStyle = function(properties)
{
    var match;
    for(var i=0; i<this.style.length; i++)
    {
		//status("Considering style: " + i);
        // rule is an array of allowed rules for this style 
        // each rule is a series of key/value pairs necessary to match 
        for(var j=0; j<this.style[i].rules.length; j++)
        {
			//status("Considering style: " + i + " rule " + j);
            match=0;
			nkeys=0;
            for(k in this.style[i].rules[j])
            {
				//status("Property: " + this.style[i].rules[j][k] +" Value for feature: " + properties[k]);
                if(
                    (properties[k] && properties[k]==this.style[i].rules[j][k])

                    ||

                    (!properties[k] && this.style[i].rules[j][k]===null)
				)
                {
					//status("Match!");
                    match++;
                }
				nkeys++;
            }
			//status("Match " + match +" Nmber of criteria " + nkeys);

            if(match==nkeys)
			{
				//status("Returning hit!");
                return this.style[i];
			}
        }
    }
    return null;
}

function status(msg)
{
    document.getElementById('status').innerHTML += msg + "<br />";
}
