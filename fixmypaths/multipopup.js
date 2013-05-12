function MultiContentPopup(feature,origData)
{
    this.feature = feature;
    this.origData = origData;
}

MultiContentPopup.prototype.reset = function()
{
    this.changeContent(this.origData);
}

MultiContentPopup.prototype.refill = function(data)
{
    var masterdiv = document.createElement("div");
    masterdiv.appendChild(data);
    var a = document.createElement("a");
    a.href='#';
    a.appendChild(document.createTextNode(" Back"));
    a.onclick = this.reset.bind(this);
    masterdiv.appendChild(a);
    this.changeContent(masterdiv);
}

MultiContentPopup.prototype.changeContent = function(content)
{
    if(this.feature.unbindPopup&&
        this.feature.closePopup&&this.feature.openPopup)
    {
        this.feature.closePopup();
        this.feature.unbindPopup();
        this.feature.bindPopup(content);
        this.feature.openPopup();
    }
    else
        this.feature.bindPopup(content);
}
