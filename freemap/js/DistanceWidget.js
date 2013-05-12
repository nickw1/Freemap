function DistanceWidget(unitsId,tenthsId,changeUnitsId)
{
    this.dist=0.0;
    this.unitsId=unitsId;
    this.tenthsId=tenthsId;    
    this.changeUnitsId = changeUnitsId;
    document.getElementById(changeUnitsId).onchange =
        this.changeUnits.bind(this);
}

DistanceWidget.prototype.displayDistance = function (dist)
    {
        var intDist=Math.floor(dist%1000), decPt=Math.floor(10*(dist-intDist)), 
            displayedIntDist = (intDist<10) ? "00" : ((intDist<100) ? "0" : ""),
        unitsElem = document.getElementById(this.unitsId); 

        distTenthsElem = document.getElementById(this.tenthsId); 

        displayedIntDist += intDist;

        unitsElem.replaceChild ( document.createTextNode(displayedIntDist),
                                 unitsElem.childNodes[0] );

        distTenthsElem.replaceChild ( document.createTextNode(decPt),
                                  distTenthsElem.childNodes[0] );
    }


DistanceWidget.prototype.addDistance=function(d)
    {
        this.dist+=d;
		this.dist=(this.dist>=0) ? this.dist:0.0;
        this.displayDistance(this.dist);
    }

DistanceWidget.prototype.resetDistance=function()
    {
        this.dist = 0;
        this.displayDistance(0);
    }

DistanceWidget.prototype.addLonLatDistance=function (lon1,lat1,lon2,lat2)
    {
        var miles = (document.getElementById(this.changeUnitsId).value==
            "miles");
        this.addDistance(this.haversineDist(lon1,lat1,lon2,lat2) 
            * (miles ? 0.6214 : 1));
    }

DistanceWidget.prototype.subtractLonLatDistance=function(lon1,lat1,lon2,lat2)
{
        var miles = (document.getElementById(this.changeUnitsId).value==
            "miles");
        this.addDistance(-this.haversineDist(lon1,lat1,lon2,lat2) 
            * (miles ? 0.6214 : 1));
}

// www.faqs.org/faqs/geography/infosystems-faq
DistanceWidget.prototype.haversineDist = function(lon1,lat1,lon2,lat2)
{
    var R = 6371;
    var dlon=(lon2-lon1) * (Math.PI/180.0);
    var dlat=(lat2-lat1) * (Math.PI/180.0);
    var slat=Math.sin(dlat/2);
    var slon=Math.sin(dlon/2);
    var a1 = slat*lat;
    var a = slat*slat + Math.cos(lat1*(Math.PI/180.0))*
        Math.cos(lat2*(Math.PI/180.0))*slon*slon;
    var c = 2 * Math.asin(Math.min(1,Math.sqrt(a)));
    return R*c;
}
DistanceWidget.prototype.changeUnits= function(e)
    {
        var miles = e.target.value=="miles";
        var factor = (miles) ?  0.6214: 1.6093;
        this.dist *=factor;
        this.displayDistance(this.dist);
    }

DistanceWidget.prototype.getKm = function()
{
    return (document.getElementById(this.changeUnitsId).value=="miles") ?
        this.dist / 0.6214: this.dist;
}

DistanceWidget.prototype.getMiles = function()
{
    return (document.getElementById(this.changeUnitsId).value=="km") ?
        this.dist * 0.6214: this.dist;
}
