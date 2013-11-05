
function Freemap(lat,lon,zoom)
{

    //var testLayer = new OverlayCanvas();

    this.geojsonLayer=new L.GeoJSON(null,
					{ onEachFeature:
						(function(feature,layer)
                        {
                            layer.bindPopup (feature.properties.text);
                            layer.options.draggable = true;
                            layer.id = feature.properties.id;
                            layer.on("dragstart",this.markerDragStart.
                                bind(this));
                            layer.on("dragend",this.markerDragEnd.
                                bind(this));
                        }).bind(this) } );

    this.walkrouteLayer=new L.GeoJSON(null,
			{ onEachFeature: (function(feature,layer)
						{
                            if(feature.geometry.type=='LineString' &&
                            feature.properties.description && 
							feature.properties.title)
                            {
                            layer.bindPopup
                                ('<h3>' + feature.properties.title +'</h3>' +
                                '<p>'+feature.properties.description+'</p>');
                            }
                        }).bind(this) }
			);
			
    var footIcon = L.icon(
        {iconUrl: '/0.6/images/foot.png',
        shadowUrl: null,
        iconSize: new L.Point(16,16),
        shadowSize: null,
        iconAnchor: new L.Point(8,15),
        popupAnchor: new L.Point(2,-2) } );

    this.walkrouteStartsLayer = new L.GeoJSON(null,
				{ onEachFeature: (function(f,layer)
            		{
                		if(f.properties.description && f.properties.title)
                		{
                    		layer.bindPopup('<h1>' + f.properties.title + 
							'</h1>'
                            +'<p>'+f.properties.description+' ' +
                            '<a href="#" id="_wr_topdf">PDF</a> ' +
                            '<a href="#" id="_wr_togpx">GPX</a> ' );

                    		layer.on("click",(function(ev)
                        	{ 
                            	document.getElementById('_wr_topdf').onclick = 
                                	function() { alert('Not available yet'); };
                            	document.getElementById('_wr_togpx').onclick = 
                                	function() { 
                                    window.location=
                                    '/0.6/ws/wr.php?action=get&id='+
                                    f.properties.id+'&format=gpx' 
                                };
                            	this.wrViewMgr.loadRoute(f.properties.id);
							
                        
                        	} ).bind(this));
						}
                	} ).bind(this) 
					,
					
        			 pointToLayer: function(geojson,latlng) 
					 {
                		return new L.Marker(latlng, { icon: footIcon } )
					 }
					});


    this.map = new FreemapWidget('map',{layers:[this.geojsonLayer,
            this.walkrouteStartsLayer,this.walkrouteLayer]});

    this.wrAddMgr=new WRAddMgr(this.walkrouteLayer,'main');
    if(lat===null) 
    {
        lat = (window.localStorage && 
            window.localStorage.getItem("lat")!==null) 
            ? window.localStorage.getItem("lat") : 51.05;
    }
    if(lon===null) 
    {
        lon = (window.localStorage && 
            window.localStorage.getItem("lon")!==null) 
            ? window.localStorage.getItem("lon") : -0.72; 
    }
    if(zoom===null) 
    {
        zoom = (window.localStorage && 
            window.localStorage.getItem("zoom")!==null) 
            ? window.localStorage.getItem("zoom") : 14;
    }

    var startPos= new L.LatLng(lat,lon);
    //this.map.setView(new L.LatLng(lat,lon),zoom).addLayer(this.kothic);
    this.map.setView(new L.LatLng(lat,lon),zoom);


    this.map.on('dragend',this.onDragEnd.bind(this));


    this.map.on('click',this.onMapClick,this);

    this.walkrouteLayer.on("featureparse", function(e)
                        {
                            if(e.geometryType=='LineString' &&
                            e.properties.description && e.properties.title)
                            {
                            e.layer.bindPopup
                                ('<h3>' + e.properties.title +'</h3>' +
                                '<p>'+e.properties.description+'</p>');
                            }
                        }
                    );

    this.wrViewMgr = new WRViewMgr(this);

    this.walkrouteStartsLayer.on("featureparse",(function(e)
            {
                if(e.properties.description && e.properties.title)
                {
                    e.layer.bindPopup('<h1>' + e.properties.title + '</h1>'
                            +'<p>'+e.properties.description+' ' +
                            '<a href="#" id="_wr_topdf">PDF</a> ' +
                            '<a href="#" id="_wr_togpx">GPX</a> ' );
                    e.layer.on("click",(function(ev)
                        { 
                            document.getElementById('_wr_topdf').onclick = 
                                function() { alert('Not available yet'); };
                            document.getElementById('_wr_togpx').onclick = 
                                function() { 
                                    window.location=
                                    '/0.6/ws/wr.php?action=get&id='+
                                    e.properties.id+'&format=gpx' 
                                };
                            this.wrViewMgr.loadRoute(e.properties.id);
                        
                        } ).  bind(this));
                }
            }).bind(this) );


    new SearchWidget ('searchdiv',
                        { url: '/0.6/ws/search.php',
                          callback: this.setLocation.bind(this),
                          parameters: 'poi=all&outProj=4326' } );
    this.ajax=new Ajax();

    this.dlg=new Dialog('main',
        { ok: this.dlgOkPressed.bind(this), 
            cancel: this.dlgCancelPressed.bind(this) },
        { backgroundColor: '#8080ff',
            width: '400px' ,
            height: '300px',
            color: 'white',
            borderRadius: '15px' }
            );
    
    this.dlg.setContent 
        ("<p>Please enter details of the annotation: </p>"+
            "<p><textarea id='annotation' " +
            "style='margin: 10px 10px 10px 10px;width:360px;height:150px'>"+
            "</textarea></p>" );

    this.walkroutesDlg=new Dialog('main',
        { ok: (function(){ this.walkroutesDlg.hide(); } ).bind(this) },
        { backgroundColor: '#8080ff',
            width: '200px' ,
            height: '150px',
            color: 'white',
            opacity: '0.5',
            borderRadius: '15px' }
            );
    this.walkroutesDlg.setPosition(100,100);
    this.mode = 0;

    //this.curWalkroute=new Array();

    this.wrPointCount = 1;

    this.modes = [ { name: "Normal" },
                    { name: "Walk route",
                        submenu:  [
                            { name: "New", action:this.wrAddMgr.newWR.bind
                                (this.wrAddMgr) },
                            { name: 'Undo last point',
                                action: this.wrAddMgr.undo.bind
                                    (this.wrAddMgr) },
                            { name: 'Add stage descriptions', 
                                action: (function(e)
                                    { 
                                        this.wrAddMgr.mode=
                                            e.target.firstChild.nodeValue==
                                            'Add stage descriptions' ? 1:0;
                                        e.target.firstChild.nodeValue =
                                            e.target.firstChild.nodeValue ==
                                            'Add stage descriptions' ?
                                            'Finish adding stages' :
                                            'Add stage descriptions';
                                    } 
                                    ).bind(this) },
                            { name: "Save", action:this.wrAddMgr.wrDone.bind
                                (this.wrAddMgr) },
                                ]
                    } ]

    this.setupModes();


    if(document.getElementById('myroutes'))
    {
        document.getElementById('myroutes').onclick=this.wrViewMgr.sendRequest.
            bind(this.wrViewMgr);
    }

    this.annotationLoader = new FeatureLoader
        ('/0.6/ws/bsvr.php',
        this.geojsonLayer,
        'inProj=4326&outProj=4326&ann=1&format=geojson');

    this.walkrouteStartsLoader = new FeatureLoader
        ('/0.6/ws/wr.php',
        this.walkrouteStartsLayer,
        'action=getByBbox&format=json');

    this.annotationLoader.loadFeatures(this.map.getBounds());
    this.walkrouteStartsLoader.loadFeatures(this.map.getBounds());

    if(document.getElementById('loginbtn'))
    {
        document.getElementById('loginbtn').onclick =
            this.doLogin.bind(this);
    }

    this.loggedIn = false;

    this.map.on("viewreset", (function(e) 
        { 
            if(this.map.getZoom()<=13)
                this.map.removeLayer(this.geojsonLayer);
            else
                this.map.addLayer(this.geojsonLayer);

            if(this.map.getZoom()<=11)
                this.map.removeLayer(this.walkrouteStartsLayer);
            else
                this.map.addLayer(this.walkrouteStartsLayer);

        } ).  bind(this) );

    this.trash = document.createElement('img');
    this.trash.src='images/trash2_b.png';
    this.trash.style.zIndex = 999;
    this.trash.style.position = 'absolute';
    this.trash.style.left = '20px';
    this.trash.style.top = '90%';
    this.trash.style.visibility = 'hidden';
    
    document.getElementById('main').appendChild(this.trash);

    window.onpopstate = (function(e)
    {
        this.setLocation(e.state.x/1000000,e.state.y/1000000);
    }).bind(this);
}

Freemap.prototype.setLoggedIn = function(status)
{
    this.loggedIn = status;
}

Freemap.prototype.dlgOkPressed = function(e)
{
    e.stopPropagation();
    this.ajax.sendRequest('/0.6/ws/annotation.php',
                            { method: 'POST',
                            parameters: 'action=create&text=' +
                                document.getElementById('annotation').value+
                                '&lat='+this.mapClickPos.lat+'&lon='+
                                    this.mapClickPos.lng,
                              callback: this.addAnnCallback.bind(this) },
                              this.mapClickPos
                        );
}

Freemap.prototype.dlgCancelPressed=function(e)
{
    this.dlg.hide();
    this.map.on('click',this.onMapClick,this);
    e.stopPropagation();
}

Freemap.prototype.addAnnCallback = function(xmlHTTP,latlng)
{
    alert('annotation added with ID: ' + xmlHTTP.responseText);
    var marker = new L.Marker(latlng, {clickable:true, draggable:true});
    marker.bindPopup(document.getElementById('annotation').value);
    marker.on("dragstart",this.markerDragStart.bind(this));
    marker.on("dragend",this.markerDragEnd.bind(this));
    marker.id = xmlHTTP.responseText;
    this.geojsonLayer.addLayer(marker);
    this.annotationLoader.indexedFeatures[xmlHTTP.responseText] = marker;
    this.dlg.hide();
    this.map.on('click',this.onMapClick,this);
}

Freemap.prototype.onMapClick = function(e)
{
    var p = this.map.layerPointToContainerPoint
        (this.map.latLngToLayerPoint(e.latlng));
    this.mapClickPos=e.latlng;

    switch(parseInt(this.mode))
    {
        case 0:
            if(this.loggedIn===true)
            {
                this.dlg.setPosition((p.x<500 ? p.x:500),(p.y<500 ? p.y:500));
                if(!this.dlg.isVisible())
                {
                    this.dlg.show();
                    this.map.off('click',this.onMapClick,this);
                }
                document.getElementById('annotation').focus();
            }
            else
            {
                alert("Must be logged in to add annotation.");
            }
            break;

        case 1:
            this.wrAddMgr.handleClick(e.latlng);
            break;
    }
}

Freemap.prototype.setLocation = function(x,y)
{
    this.map.panTo(new L.LatLng(y,x));
    this.saveLocation();
}

Freemap.prototype.onDragEnd=function(e)
{
    var x=Math.round(this.map.getCenter().lng*1000000),
        y=Math.round(this.map.getCenter().lat*1000000);
    //history.pushState({x:x,y:y},"","?x="+x+"&y="+y);
    this.annotationLoader.loadFeatures(e.target.getBounds());
    this.walkrouteStartsLoader.loadFeatures(e.target.getBounds());
    this.saveLocation();
    this.getImage();
}

Freemap.prototype.saveLocation = function()
{
    if(window.localStorage)
    {
        window.localStorage.setItem("lat",this.map.getCenter().lat);
        window.localStorage.setItem("lon",this.map.getCenter().lng);
        window.localStorage.setItem("zoom",this.map.getZoom());
    }
}

Freemap.prototype.setupModes = function()
{
    var div = document.getElementById("modebar");
    for(var i=0; i<this.modes.length; i++)
    {
        var modeDiv = document.createElement("a");
        modeDiv.href='#';
        modeDiv.id = "_mode" + i;
        modeDiv.innerHTML = this.modes[i].name;
        modeDiv.onclick = function(e)
        {
            document.getElementById("_mode"+this.mode).removeAttribute("class");
            e.target.setAttribute("class","selected");
            var oldmode=this.mode;
            this.mode = e.target.id.substring(5);
            if(this.mode==1 && oldmode!=1)
            {
                this.map.removeLayer(this.geojsonLayer);
                this.map.removeLayer(this.walkrouteStartsLayer);
            }
            else if (oldmode==1 && this.mode!=1)
            {
                this.map.addLayer(this.geojsonLayer);
                this.map.addLayer(this.walkrouteStartsLayer);
            }
        }.bind(this);
        if(this.modes[i].submenu)
        {
            var childDiv=document.createElement("div");
			childDiv.onclick = function (e)
			{
				e.stopPropagation();
			};

            for(var j=0; j<this.modes[i].submenu.length; j++)
            {
                var a = document.createElement("a");
                a.href='#';
                a.id = "_mode"+i+"_opt"+j;
                a.innerHTML = this.modes[i].submenu[j].name;
                a.onclick = ( function(e)
                                {
                                    var m=e.target.id[5];
                                    var n = e.target.id[10];
                                    e.stopPropagation();
                                    this.modes[m].submenu[n].action(e);
                                }
                            ).bind(this);
                childDiv.appendChild(a);
                childDiv.appendChild(document.createElement("br"));
            }
            modeDiv.appendChild(childDiv);
        }
        div.appendChild(modeDiv);
    }
    document.getElementById("_mode"+this.mode).setAttribute("class","selected");
}

Freemap.prototype.doLogin = function()
{
    this.ajax.sendRequest('user.php',
                { parameters: 'action=login&remote=1&username='+
                    document.getElementById('username').value + 
                    '&password='+ 
                    document.getElementById('password').value ,
                    method : 'POST',
                    callback: this.loginSuccess.bind(this),
                    errorCallback: function(code){alert('Incorrect login'); } }
                    );
}

Freemap.prototype.loginSuccess = function(xmlHTTP)
{
    document.getElementById('logindiv').innerHTML = 
        '<em>Logged in as ' + xmlHTTP.responseText + 
        '</em> <a href="#" id="myroutes">My routes</a> | '+
        '<a href="user.php?action=logout&redirect=index.php">Log out</a>';
    document.getElementById('myroutes').onclick = this.wrViewMgr.sendRequest.
        bind(this.wrViewMgr);
    this.loggedIn = true;
}

Freemap.prototype.getImage = function()
{
    /*
    var base64 = this.kothic.toDataURL();
    alert(base64.length);
    var raw = atob(base64.split(",")[1]);
    alert(raw);
    */
    //document.getElementById('results').appendChild(document.createTextNode(base64));
    /*
    this.ajax.sendRequest
        ('/0.6/ws/wr.php',
            { method: 'POST',
              parameters: 'data='+base64+'&action=postImage',
              callback: function() { alert('done'); } 
             }
         );
    */
}

Freemap.prototype.markerDragStart= function(e)
{
    this.trash.style.visibility = 'visible';
    e.target.dragStart = e.target.getLatLng();
}

Freemap.prototype.markerDragEnd = function(ev)
{


    var p = this.map.layerPointToContainerPoint
        (this.map.latLngToLayerPoint(ev.target.getLatLng()));

    var trashTop = 0.9 * this.map.getSize().y;

    if(p.x>=20 && p.x<=51 && p.y>=trashTop && p.y<=trashTop+63)
    {
        this.ajax.sendRequest('/0.6/ws/annotation.php',
                                { method: 'POST',
                                  parameters: 'action=delete&id='+
                                      ev.target.id,
                                 callback: (function()
                                     { this.geojsonLayer.removeLayer
                                        (ev.target) }
                                    ).bind(this),
                                 errorCallback: function(code)
                                     { 
                                        if(code==401)
                                            alert('Need to login to delete.');
                                        else
                                            alert('Server error:' +code);
                                         ev.target.setLatLng
                                            (ev.target.dragStart);
                                     }
                                }
                            );
    }
    else
    {
        this.ajax.sendRequest('/0.6/ws/annotation.php',
                            { method: 'POST',
                              parameters: 'action=move&lat=' +
                              ev.target.getLatLng().lat+'&lon='+
                              ev.target.getLatLng().lng + '&id='+
                              ev.target.id,
                              callback: function(xmlHTTP) { } ,
                              errorCallback: function(err)
                                  {
                                    if(err==401)
                                        alert('Need to login to move markers.');
                                    else
                                        alert('Server error: ' + err);
                                    ev.target.setLatLng(ev.target.dragStart);
                                }
                            }
                        );
    }
    this.trash.style.visibility = 'hidden';
}

function init()
{
    var freemap = new Freemap(lat,lon,zoom);
    freemap.setLoggedIn(loggedIn);
}
