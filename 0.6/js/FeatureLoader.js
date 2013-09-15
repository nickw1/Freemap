
function FeatureLoader(url,layer,extraURLParameters)
{
    this.url=url;
    this.layer=layer;
    this.extraURLParameters = extraURLParameters;
    this.ajax = new Ajax();
    this.indexedFeatures = new Array();
}

FeatureLoader.prototype.loadFeatures=function(bounds)
{
    var qs ='bbox=' +
            bounds.getSouthWest().lng+ ',' + 
            bounds.getSouthWest().lat+ ',' + 
            bounds.getNorthEast().lng+ ',' + 
            bounds.getNorthEast().lat + '&' + this.extraURLParameters;
            //inProj=4326&outProj=4326' +'&ann=1&format=geojson';
    this.ajax.sendRequest 
        (this.url,
            { parameters: qs, callback: this.processFeatures.bind(this) });
}

FeatureLoader.prototype.processFeatures=function(xmlHTTP)
{
	var json=JSON.parse(xmlHTTP.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        var c = json.features[i].geometry.coordinates;
        if (!this.indexedFeatures[json.features[i].properties.id])
        {
            this.indexedFeatures[json.features[i].properties.id]=
                    json.features[i];
            this.layer.addData(json.features[i]);
        }
    }
}
