function FixMyPaths(lon,lat)
{
    var crs = 
        L.CRS.proj4js('EPSG:27700',
             "+proj=tmerc +lat_0=49 +lon_0=-2 "+
             "+k=0.9996012717 +x_0=400000 +y_0=-100000 "+
             "+ellps=airy +datum=OSGB36 +units=m +no_defs",
             new L.Transformation(1, 0, -1, 0) );
    crs.scale = function(){ return 0.2; } 

    this.map = new L.Map("map", { crs: crs} );

    this.indexedFeatures = new Array();
    this.indexedProblems = new Array();

    var osLayer = new L.TileLayer 
        ("http://www.free-map.org.uk/lfp/{z}/{x}/{y}.png",
        { tms:true, maxZoom:0, minZoom:0, tileSize: 200, continuousWorld:true,
            attribution:"&copy; Ordnance Survey, OS OpenData Licence; "+
			 "footpath data: &copy; Hampshire County Council, "+
			 "Open Government Licence" } ); 

    this.map.addLayer(osLayer);


    this.map.setView(new L.LatLng(lat, lon), 0);

    this.rowLayer = new L.GeoJSON ( null,
            {onEachFeature: (function(feature,layer)
                {
                    var styles = { 'Footpath': '#c000ff',
                                'Bridleway': '#00ff00',
                                'BOAT' : '#ff0000',
                                'Restricted Byway': '#0000ff' };

                    layer.setStyle({color:styles[feature.properties.row_type]});
                    var p = document.createElement("p");
                    p.appendChild(document.createTextNode
                        (feature.properties.parish_row));
                    var a =document.createElement("a");
                    a.href ='#';
                    a.appendChild(document.createTextNode(" Report problem"));
                    p.appendChild(a);
                    var mp= new MultiContentPopup(layer,p);
                    a.onclick= (function(id)
                    {
                        return this.showProblemDialog.bind
                            (this,id,mp);
                    }).call(this,feature.properties.gid);
                    // won't work in IE 9 with vanilla Leaflet
                    // see https://github.com/CloudMade/Leaflet/issues/695
                    mp.reset();
                    layer.on("click", (function(e) 
                        { this.rowClickedPos = e.latlng; }).bind(this) );
                }).bind(this)
            }
        );

    this.map.addLayer(this.rowLayer);

    this.reportedProblems = new L.GeoJSON ( null,
            { onEachFeature: (function(feature,layer)
                {
                    var p = document.createElement("p");
                    p.appendChild(document.createTextNode
                            (feature.properties.problem));
					if(feature.properties.status!="Fixed")
					{
                    	var a = document.createElement("a");
                    	a.href='#';
                    	a.appendChild(document.createTextNode(" Updates"));
                    	p.appendChild(a);
                    	var mp = new MultiContentPopup(layer,p);
                    	a.onclick = (function(id)
                             {
                                 return this.getProblemLog.bind
                                     (this,id,mp); 
                             }
                                ).call(this,feature.properties.id);
                    	mp.reset();
					}
					else
					{
						var strong =  document.createElement("strong");
						strong.appendChild(document.createTextNode(" FIXED!"));
						p.appendChild(strong);
                    	var mp = new MultiContentPopup(layer,p);
                    	mp.reset();
					}
                    this.indexedProblems[feature.properties.id]=layer;
                }).bind(this)
            } );


    this.map.addLayer(this.reportedProblems);
    this.map.on("dragend", this.loadFeatures.bind(this));

    this.ajax = new Ajax();
    this.markersAjax = new Ajax();

    this.osgb = new Proj4js.Proj("EPSG:27700");
    this.wgs84 = new Proj4js.Proj("EPSG:4326");
    this.sphmerc = new Proj4js.Proj("EPSG:900913");

    this.loadFeatures();
}

FixMyPaths.prototype.loadFeatures = function(ev)
{
    var w = this.map.getBounds().getSouthWest().lng;
    var s = this.map.getBounds().getSouthWest().lat;
    var e = this.map.getBounds().getNorthEast().lng;
    var n = this.map.getBounds().getNorthEast().lat;

    var sw = new Proj4js.Point(w,s);
    var ne = new Proj4js.Point(e,n);


    this.ajax.sendRequest('hampshire.php',
            { method: 'GET',
              parameters: 
                'bbox='+sw.x+','+sw.y+','+ne.x+','+ne.y+
                '&inProj=4326&outProj=4326',
              callback: this.addGeoJSON.bind(this)        
              }
              );

    this.markersAjax.sendRequest('problem.php',
                    { method: 'GET',
                      parameters: 'action=getAllProblems&inProj=4326&bbox='+
                              sw.x+','+sw.y+','+ne.x+','+ne.y+'&outProj=4326',
                                callback: this.addProblemGeoJSON.bind(this) }
                        );
}

FixMyPaths.prototype.addGeoJSON = function(xmlHTTP)
{
    var json = JSON.parse(xmlHTTP.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        if(!(this.indexedFeatures[json.features[i].properties.gid]))
        {
            this.rowLayer.addData(json.features[i]);
            this.indexedFeatures[json.features[i].properties.gid] =
                        json.features[i];

        }
    }
}

FixMyPaths.prototype.addProblemGeoJSON = function(xmlHTTP)
{
    var json = JSON.parse(xmlHTTP.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        if(!this.indexedProblems[json.features[i].properties.id])
        {
            this.reportedProblems.addData(json.features[i]);
        }
    }
}

FixMyPaths.prototype.showProblemDialog = function(gid,mp)
{
    var problemTypes = ["Structure - Stile",
                        "Structure - Gate",
                        "Structure - Squeeze Gap",
                        "Structure - Steps",
                        "Structure - Bridge",
                        "Structure - Boardwalk",
                        "Obstruction - Fence",
                        "Obstruction - Locked Gate",
                        "Obstruction - Vegetation ",
                        "Obstruction - Fallen Tree",
                        "Obstruction - Crops",
                        "Obstruction - Ploughed Field",
                        "Animals",
                        "Sign Posting - Fingerpost",
                        "Sign Posting - Waymarking",
                        "Surface",
                        "Other"];
    var content="<p>Please enter the problem:<br />" +
                "<textarea id='theproblem' " +
                "style='margin:10px 10px 10px 10px; display:block;width:200px;height:200px'>"+
                "</textarea></p><p>Please select the problem type:<br />";
    content += "<select id='category'>";
    for(var i=0; i<problemTypes.length; i++)
        content += "<option>"+problemTypes[i]+"</option>";
    content += "</select></p>";
    content += "<p>Your name: <br /><input id='reporter_name' /><br /> "+
            "Your email: <br /><input id='reporter_email' /></p>";
    var div = document.createElement("div");
    div.innerHTML = content;
    var ok = document.createElement("input");
    ok.type="button";
    ok.value="OK";
    ok.onclick = this.doSendProblem.bind(this,gid,mp)
    var masterdiv = document.createElement("div");
    masterdiv.appendChild(div);
    masterdiv.appendChild(ok);
    mp.refill(masterdiv);
}

FixMyPaths.prototype.cancelSendProblem = function()
{
    //this.dlg.hide();
}


FixMyPaths.prototype.doSendProblem = function(gid,mp)
{
    if(document.getElementById('reporter_name').value.length==0 ||
       document.getElementById('reporter_email').value.length==0)
    {
        alert("Please enter a name and email!");
    }
    else
    {
        this.ajax.sendRequest ( 'row.php',
                        { method: 'POST',
                         parameters: 'action=addProblem&x=' + 
                             this.rowClickedPos.lng
                                 +'&y=' +this.rowClickedPos.lat + '&problem=' +
                                document.getElementById('theproblem').value +
                                '&id=' + gid + '&category=' +
                                document.getElementById('category').value +
                                '&reporter_name='+
                                document.getElementById('reporter_name').value +
                                '&reporter_email='+
                                document.getElementById('reporter_email').value,
                        callback: (function(xmlHTTP) 
                            { alert(xmlHTTP.responseText); 
                                mp.reset();
                              this.loadFeatures();}).bind(this)
                        }
                    );
    }
}

FixMyPaths.prototype.setLocation = function(x,y)
{
    this.map.panTo(new L.LatLng(y,x));
    this.loadFeatures();
}

FixMyPaths.prototype.moveToProblem = function(id)
{
    if(this.indexedProblems[id])
    {
        this.displayProblemMarker
            (this.indexedProblems[id].getLatLng().lat,
             this.indexedProblems[id].getLatLng().lng);
    }
    else
    {
        new Ajax().sendRequest 
            ("problem.php",
                    { method: 'GET',
                      parameters: 'action=getLocation&id='+id,
                      callback: (function(xmlHTTP)
                                      {
                                        var json=JSON.parse
                                            (xmlHTTP.responseText);
                                        if(json[0]!="0" && json[1]!="0")
                                            this.displayProblemMarker
                                                (json[1],json[0]);
                                      }
                                      ).bind(this)
                    });
    }
}

FixMyPaths.prototype.displayProblemMarker = function(lat,lon)
{
        var ll = new L.LatLng(lat,lon);
        this.setLocation(ll.lng,ll.lat);
}

FixMyPaths.prototype.getProblemLog = function(id,mp)
{
    new Ajax().sendRequest ('problem.php',
                            { method: 'GET',
                              parameters: 'action=getLog&id=' + id,
                              callback: this.getProblemLogCallback.bind(this) },
                                [id,mp]
                            );
}

FixMyPaths.prototype.getProblemLogCallback = function(xmlHTTP,addData)
{
    var id = addData[0], mp = addData[1];
    var json = JSON.parse(xmlHTTP.responseText);
    var p = document.createElement("p");
    var ul = document.createElement("ul");
    if(json.length>0)
    {
        for(var i=0; i < json.length; i++)
        {
            var li = document.createElement("li");
            li.appendChild(document.createTextNode(json[i].log));
            li.appendChild(document.createTextNode(" "));
            var em = document.createElement("em");
            em.appendChild(document.createTextNode("Added "+json[i].subdate));
            li.appendChild(em);
            ul.appendChild(li);
        }
        p.appendChild(ul);
    }
    else
        p.appendChild(document.createTextNode("No updates so far"));
    var a = document.createElement("a");
    a.href='#';
    a.appendChild(document.createTextNode(" Add update"));
    a.onclick =    function()
        {
            var content="<p>Please enter the update:<br />" +
                "<textarea id='problemupdate' " +
                "style='margin:10px 10px 10px 10px; display:block;width:200px;height:200px'>"+
                "</textarea></p>";
            var div = document.createElement("div");
            div.innerHTML = content;
            var masterdiv = document.createElement("div");
            masterdiv.appendChild(div);
            var btn = document.createElement("input");
            btn.type='button';
            btn.value='OK';
            btn.onclick = this.doSendProblemUpdate.bind(this,id,mp);
            masterdiv.appendChild(btn);
            mp.refill(masterdiv);
        }.bind(this);
    p.appendChild(a);
    mp.refill(p);
}

FixMyPaths.prototype.doSendProblemUpdate = function(id,mp)
{
    var msg = document.getElementById('problemupdate').value;
    alert("doSendProblemUpdate: id="+id+" msg="+msg );
    new Ajax().sendRequest('problem.php',
                             { method: 'POST',
                              parameters: 'msg='+ msg
                                +'&id='+id+'&action=addToLog',
                             callback: 
                                 (function() 
                                     { 
                                        alert("done");
                                        mp.reset();
                                    }).bind(this)
                             } );
}


function init()
{
    var app = new FixMyPaths(lon,lat);
    if(probid>0)
        app.moveToProblem(probid);
    new Reports("reports","problem.php",180,app.moveToProblem.bind(app)).load();
    new SearchWidget ('search',
                        { url: 'http://www.free-map.org.uk/freemap/ws/'+
							'search.php',
                          callback: app.setLocation.bind(app),
						  parameters: 'poi=all&outProj=4326'} 
                          );
}
