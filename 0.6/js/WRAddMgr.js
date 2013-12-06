function Waypoint(mLL)
{
    if( typeof Waypoint.prototype.count == "undefined")
        Waypoint.prototype.count = 0;
    this.id=++Waypoint.prototype.count;
    var WpIcon = L.icon (
        { iconUrl:'http://www.free-map.org.uk/0.6/flag.php?'
        +'n='+this.id,
        shadowUrl:null,
        iconSize: new L.Point(32,32),
        shadowSize: null,
        iconAnchor: new L.Point(0,32),
        popupAnchor: new L.Point (2,-2)
        } );
    L.Marker.call(this,mLL,
        {icon: WpIcon, clickable:true,draggable:true});
}

Waypoint.prototype = Object.create(L.Marker.prototype); 

Waypoint.prototype.addDescription = function(d)
{
    this.description=d;
}

Waypoint.prototype.resetMarkerCount = function()
{
    Waypoint.prototype.count = 0;
}

Waypoint.prototype.toGeoJSONObj = function()
{
    var json=new Object();
    json.geometry=new Object();
    json.geometry.coordinates = [ this.getLatLng().lng,
                                    this.getLatLng().lat ];
    json.properties = new Object();
    json.properties.id = this.id;
    json.properties.description = this.description;
    return json;
}

function WRAddMgr(wrLayer,divId)
{
    this.walkrouteLayer = wrLayer;
    this.distanceWidget = new DistanceWidget('units','tenths','distUnits');
    this.clearWalkroute();
    this.ajax = new Ajax();
    this.sendDlg = new Dialog ( divId,
         { ok: this.doSendWR.bind(this),
          cancel: this.cancelSendWR.bind(this) },
        { backgroundColor: '#8080ff',
            width: '400px' ,
            height: '400px',
            color: 'white',
            borderRadius: '15px' }
        );
    this.sendDlg.setContent  
        ("<h3>Please enter details of your walk:</h3><p>Walk title: "+
            "<input id='_wrmgr_wtitle' /></p>"+
            "<p>Description:<br /><textarea id='_wrmgr_wdesc' " +
            "style='margin: 10px 10px 10px 10px;width:360px;height:150px'>"+
            "</textarea></p>" );

    this.wptDlg = new Dialog ( divId,
         { ok: (function() { 
                    this.waypoints[this.curWp-1].description=
                        document.getElementById('_wrmgr_wptext').value;
                    this.wptDlg.hide();
                          } ).bind(this),
          cancel: (function() { this.wptDlg.hide(); }).bind(this) },
        { backgroundColor: '#8080ff',
            width: '400px' ,
            height: '300px',
            color: 'white',
            borderRadius: '15px' }
        );

    this.sendDlg.setPosition("200px","200px");
    this.wptDlg.setPosition("200px","200px");
    this.points=new Array();
}

WRAddMgr.prototype.handleClick = function(point)
{

    switch(this.mode)
    {
        case 0:
            if(this.lastPoint!==null)
            {
                this.distanceWidget.addLonLatDistance
                    (this.lastPoint.lng,this.lastPoint.lat,
                    point.lng,point.lat);
                this.curPolyline.addLatLng(point);
                this.curPolyline.redraw();
            }
            else
            {
                this.points.push(point);
                this.curPolyline = new L.Polyline(this.points,
                                            {clickable:false});
                this.walkrouteLayer.addLayer(this.curPolyline);
            }
            var p = new L.Circle ( point, 10, { clickable: false,
                fillOpacity: 0.2 } );
            this.walkrouteLayer.addLayer(p);
            this.lastPoint = point;
            break;

        case 1:
            this.addWaypoint(point);
            break;
    }
}


WRAddMgr.prototype.addWaypoint = function(mLL)
{
    var wp = new Waypoint(mLL);
    this.waypoints.push(wp);
    
    // click gives the geojson layer not the marker if setup after adding to
    // geojson layer
    wp.on("click",(function(e) 
        { 
            this.showStageDetailsDialog(e.target);
        } 
    ).bind(this));

    this.showStageDetailsDialog(wp);
    this.walkrouteLayer.addLayer(wp);
}

WRAddMgr.prototype.undo = function()
{
    this.walkrouteLayer.clearLayers();
    if(this.points.length>=2)
    {
        this.distanceWidget.subtractLonLatDistance
            (this.points[this.points.length-2].lng,
            this.points[this.points.length-2].lat,
            this.points[this.points.length-1].lng,
            this.points[this.points.length-1].lat);
        this.points.pop();
        this.walkrouteLayer.addLayer
            (new L.Circle(this.points[0],10,{clickable:false}));
        for(var i=1; i<this.points.length; i++)
        {
            this.walkrouteLayer.addLayer
                (new L.Circle(this.points[i],10,{clickable:false}));
        }
        this.curPolyline = new L.Polyline(this.points);
        this.walkrouteLayer.addLayer(this.curPolyline);

        for(var i=0; i<this.waypoints.length; i++)
        {
            this.walkrouteLayer.addLayer(this.waypoints[i]);
        }

        this.lastPoint = this.points[this.points.length-1];
    }
}

WRAddMgr.prototype.newWR = function()
{
    this.clearWalkroute();
}

WRAddMgr.prototype.wrDone = function()
{
    this.sendDlg.show();
}

WRAddMgr.prototype.doSendWR = function()
{
    var json = new Object();
    json.type = 'FeatureCollection';
    var route = new Object();
    route.type = 'Feature';
    route.properties = new Object();
    route.properties.isRoute = 'yes';
    route.properties.title = document.getElementById('_wrmgr_wtitle').value; 
    route.properties.description=document.getElementById('_wrmgr_wdesc').value;
    route.properties.distance = this.distanceWidget.getKm();
    route.geometry = new Object();
    route.geometry.type='LineString';
    route.geometry.coordinates = [];
    for(var i=0; i<this.points.length; i++)
    {
        route.geometry.coordinates.push( [ this.points[i].lng,
                                this.points[i].lat ] );
    }
    json.features = [];
    json.features.push(route);
    for(var i=0; i<this.waypoints.length; i++)
    {
        json.features.push(this.waypoints[i].toGeoJSONObj());
    }


    this.ajax.sendRequest('/0.6/ws/wr.php',
                            { parameters: 'action=add&route=' +
                                      JSON.stringify(json),
                              method: 'POST',
                              callback: this.wrAdded.bind(this),
                              errorCallback: this.wrAddedError.bind(this) }
                        );
}

WRAddMgr.prototype.cancelSendWR = function()
{
    this.sendDlg.hide();
}

WRAddMgr.prototype.wrAdded = function(xmlHTTP)
{
    this.sendDlg.hide();
    alert('route added with ID: ' + xmlHTTP.responseText);
}

WRAddMgr.prototype.wrAddedError = function(status)
{
    if(status==401)
        alert('Please login');
    else
        alert('Server error: HTTP code=' + status);
}


WRAddMgr.prototype.clearWalkroute = function()
{
    this.points=[];
    this.waypoints=[];
    this.walkrouteLayer.clearLayers();
    this.prevWpId=null;
    this.lastPoint=null;
    this.mode=0;
    this.distanceWidget.resetDistance();
    Waypoint.prototype.resetMarkerCount();
}

WRAddMgr.prototype.showStageDetailsDialog = function(wp)
{
    this.wptDlg.show();
    this.curWp = wp.id; 
    var contents = (this.waypoints[this.curWp-1].description) ?
                this.waypoints[this.curWp-1].description: '';
    this.wptDlg.setContent('Enter details for stage ' + this.curWp +
                            ' of the walk: <br /> ' +
                            '<textarea id="_wrmgr_wptext" ' +
                             'style="margin: 10px 10px 10px 10px;'+
                            'width:360px;height:150px">'+contents+
                            '</textarea>');
}
