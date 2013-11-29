
function OTV(contid, switcherid, logindivid, freemapRoot)
{
    this.mapid="otv_map";
    this.contid=contid;

    this.switcher = document.getElementById(switcherid);
    this.logindiv = document.getElementById(logindivid);

    this.lat = HttpData.lat;
    this.lon = HttpData.lon;

    this.curPhotosphereId = 0;
    this.ajax = new Ajax();

    this.freemapRoot = freemapRoot;
    
    this.pDiv = document.createElement("div");
    this.pDiv.setAttribute("style",
    "position:absolute;top:0px;left:0px;width:100%; height:100%; z-index:1");
	this.pDiv.setAttribute("id","panodiv");

    this.switcher.addEventListener("click",this.switchMode.bind(this,true) , 
                                        false);

    this.mDiv = document.createElement("div");
    this.mDiv.setAttribute("id", this.mapid);
    this.mDiv.setAttribute("style",
    "position:absolute; top:0px; left:0px; width:100%; height:100%;z-index:2");

    if(HttpData.panoId>0 || HttpData.getNearest==true)
    {
        this.mode = 1;
        this.switcher.innerHTML = "map";
        document.getElementById("commands").style.left="0px";
        document.getElementById(this.contid).appendChild(this.pDiv);
        if(HttpData.getNearest == true)
            this.findNearestPhotosphere();
        else
            this.loadPhotosphereById(HttpData.panoId);
    }
    else
    {
        this.mode = 0;
        this.switcher.innerHTML = "pano view";
        document.getElementById(this.contid).appendChild(this.mDiv);
        document.getElementById("commands").style.left="50px";
    }


    var uploadProcessor = 
         function()
         {
            var file = document.getElementById("file1").files[0];
            var formData = new FormData();
            formData.append("file1", file);
            var xhr2 = new XMLHttpRequest();
            xhr2.addEventListener("load",
                function(e)
                {
                    document.getElementById("progress2").innerHTML = 
                        e.target.responseText;
                } ,false);
            xhr2.addEventListener("error",
                function(e) { alert(e.target) },false);
            xhr2.addEventListener("abort",
                function(e) { alert(e.target) },false);
            xhr2.upload.addEventListener("progress",
                function(e)
                {
                    var pct = Math.round (e.loaded/e.total * 100);
                    document.getElementById("progress2").innerHTML =
                        "Uploaded : " + e.loaded + " total:" + e.total + 
                        " ("+pct+"%)";
                    document.getElementById("progress1").value=Math.round(pct);
                } ,false);
            xhr2.open("POST", "panosubmit.php");
            xhr2.send(formData);
        };

    this.uploadDlg =  new Dialog (contid,
                        { "Upload": uploadProcessor,
                            "Close": 
                            (function()
                                { 
                                    this.uploadDlg.hide();
                                }).bind(this)
                        },
                        { backgroundColor: "rgba(128,128,255,0.5)",
                          color: "white",
                          borderRadius: "20px" }
                           );
    
    this.uploadDlg.setContent(
        '<h2>Upload photospheres</h2>' +
        '<form method="post" enctype="multipart/form-data">' +
        'Select your file (max 5MB) : <input type="file" id="file1" /> <br />' +
        '<progress id="progress1" value="0" max="100" style="width: 90%">' +
        '</progress> <br />' +
        '<span id="progress2"></span><br /></form>');

    this.uploadDlg.setPosition("25%", "25%");
    this.uploadDlg.setSize ("50%", "50%");

    this.showUploadDlg = document.createElement("a");
    this.showUploadDlg.setAttribute("href","#");
    this.showUploadDlg.setAttribute("class","panolink");
    this.showUploadDlg.innerHTML = "Upload photospheres";
    this.showUploadDlg.addEventListener ("click",
            (function() { 
                    this.uploadDlg.show();
                        }).bind(this), false);

    this.loadMap();
    this.login = new Login ("login", "loginlink", "user.php",
                            { backgroundColor: 'rgba(192,192,255,0.8)',
                                position:'absolute',
                                top:'50px', right:'0px',
                                borderRadius: '10px'} ,
                            { onLogin: this.loginCb.bind(this),
                                onLogout: this.logoutCb.bind(this) } );

    if(SessionData.username!="")
        this.login.setLoggedIn(SessionData.username, SessionData.admin);

    var search = new NominatimWidget
                    ("search",
                        {
                            url: this.freemapRoot+'/common/'
                                +'MapquestNominatimProxy.php',
                            callback: this.setLocation.bind(this),
                            parameters: ''
                        },
                        {  backgroundColor: 'white',
                            color: 'black',
                            fontSize: '10pt',
                            position: 'absolute', top: '25px',
                            width: '100%', height: '300px' }
                    );
            
}

OTV.prototype.loadMap = function()
{
    this.map = new L.Map (this.mapid);

    var tileLayer = new L.TileLayer
                ("http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                {  attribution:
                'Map data &copy; <a href="http://www.openstreetmap.org/">'+
                'OpenStreetMap</a> contributors, Open Database Licence,'+
                'see <a href="http://www.openstreetmap.org/copyright">'+
                'copyright info</a>' } );
    this.map.addLayer(tileLayer);
    var cameraIcon = L.icon(
                { iconUrl: '/images/camera.png',
                    shadowUrl: null,
                    iconSize: new L.Point(16,16),
                    shadowSize: null,
                    iconAnchor: new L.Point(8,8),
                    popupAnchor: new L.Point(8,0) }
                        );
                    
    var panoramasLayer = new L.GeoJSON (null,
                { pointToLayer: function(geojson, latlng)
                    {
                        return new L.Marker (latlng, { icon: cameraIcon } );
                    },
            
                
                    onEachFeature: (function(feature,layer)
                    {
                        layer.on("click", (function()
                            {
                                this.loadPhotosphereById(feature.properties.id);    
                            }).bind(this)
                        );
                    }).bind(this)
                });

    this.map.addLayer(panoramasLayer);

    var loader = new FeatureLoader
                ('pano.php', panoramasLayer, 'action=getWithinBbox');
    
    this.map.setView(new L.LatLng(this.lat, this.lon),14);
    loader.loadFeatures(this.map.getBounds());

    this.map.on("dragend", (function(e)
                            {
                                loader.loadFeatures(e.target.getBounds());
                                var centre = e.target.getCenter();
                                this.lat = centre.lat; 
                                this.lon = centre.lng;
                            }).bind(this)
                        );

}

OTV.prototype.switchMode = function (doFindNearest)
{
    if(this.mode == 0)
    {
        document.getElementById(this.contid).replaceChild 
            (this.pDiv, this.mDiv);
        document.getElementById("commands").style.left="0px";
        this.switcher.innerHTML = "map";
        this.mode = 1;
        if(doFindNearest)
            this.findNearestPhotosphere();
    }
    else
    {
        document.getElementById(this.contid).replaceChild 
            (this.mDiv, this.pDiv);
        document.getElementById("commands").style.left="50px";
        this.switcher.innerHTML = "pano view";
		this.photosphere.dataURL = null;
        this.mode = 0;
        if(!this.map)
            this.loadMap();
    }
}

OTV.prototype.loginCb =  function(username, isadmin)
{
    this.logindiv.insertBefore (this.showUploadDlg, this.login.link);
    var last = this.showUploadDlg;
    if(isadmin==1)
    {
        var a = document.createElement("a");
        a.setAttribute("href", "pano.php?action=getUnmoderated");
        a.setAttribute("class","panolink");
        a.appendChild(document.createTextNode("Moderate"));
        this.logindiv.insertBefore (a, this.showUploadDlg);
        last = a;
    }
    var loggedInAs=document.createTextNode (" Logged in as " + username + " ");
    this.logindiv.insertBefore (loggedInAs, last);
        
    document.getElementById("signup").innerHTML = "";
}

OTV.prototype.logoutCb = function()
{
    while(this.logindiv.firstChild != this.login.link)
        this.logindiv.removeChild(this.logindiv.firstChild);
    this.uploadDlg.style.visibility = "hidden";
    document.getElementById("signup").innerHTML = 
        " <a href='user.php?action=signup'>Signup</a> | ";
}

OTV.prototype.loadPhotosphereById = function(id)
{
    if(this.curPhotosphereId != id)
    {
        this.loadPhotosphere ('pano/' + id + ".jpg");
        this.curPhotosphereId = id;
    }
}

OTV.prototype.loadPhotosphere = function(file)
{
    if(!this.photosphere)
        this.photosphere = new Photosphere(file);
    else
        this.photosphere.image = file;
    this.photosphere.loadPhotosphere(this.pDiv);
    if(this.mode!=1)
        this.switchMode();
}

OTV.prototype.setLocation= function(x,y)
{
    this.lat = y;
    this.lon = x;
    if(this.map)
        this.map.panTo(new L.LatLng(y,x));
}

OTV.prototype.findNearestPhotosphere = function()
{
    this.ajax.sendRequest ('pano.php',
                            { method: 'GET',
                              parameters: 'action=getNearest&lat=' +
                                    this.lat+'&lon=' + this.lon,
                              callback: (function(xmlHTTP)
                                  {
                                    if(xmlHTTP.status==200)
                                    {
                                        this.loadPhotosphereById
                                            (xmlHTTP.responseText);
                                    }
                                }
                                ).bind(this)
                            }
                          );
}

function init()
{
    // Change www.free-map.org.uk on other servers - this points to the 
    // root URL of a Freemap installation

    new OTV("container", "switcher", "loginlink", "http://www.free-map.org.uk");
}
