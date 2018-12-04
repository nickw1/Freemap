function MultiContentPopup(feature,origData)
{
    this.feature = feature;
    this.prevContent = new Array();
    this.content=origData;
    this.feature.bindPopup(origData);
}


MultiContentPopup.prototype.goBack = function()
{
    if(this.prevContent.length>0)
    {
        var prevContent = this.prevContent.pop();
        this.content = prevContent;
        if(typeof prevContent === "function")
            prevContent();
        else
            this.changeContent(prevContent);
    }
}

// Difference from changeContent(): saves current content to the stack of
// previous content
// current content can be a function

MultiContentPopup.prototype.addContent = function(content)
{
    this.prevContent.push(this.content);
    this.content = content;
    if(typeof content==="function")
    {
        content();
    }
    else
    {
        this.changeContent(content);
    }
}

// Could be called directly from a callback to a function added with
// addContent()
// Difference from doChangeContent(): creates a div with a "back" link
MultiContentPopup.prototype.changeContent = function(data)
{
    var masterdiv = document.createElement("div");
    masterdiv.appendChild(data);
    if(this.prevContent.length>0)
    {
        var a = document.createElement("a");
        a.href='#';
        a.appendChild(document.createTextNode(" Back"));
        a.onclick = this.goBack.bind(this);
            masterdiv.appendChild(a);
    }
    this.doChangeContent(masterdiv);
}

MultiContentPopup.prototype.doChangeContent = function(content)
{
    
    if(this.feature.closePopup && this.feature.unbindPopup && 
        this.feature.bindPopup && this.feature.openPopup)
    {
        this.feature.closePopup();
        this.feature.unbindPopup();
        this.feature.bindPopup(content);
        this.feature.openPopup();
    }
    else
    {
        this.feature.bindPopup(content);
    }
}
