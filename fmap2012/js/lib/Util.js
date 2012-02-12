
// Provide (simple) bind if it doesn't exist
if (! Function.prototype.bind)
{
    Function.prototype.bind = function(target)
    {
        var self=this;
        return function() { self.apply(target); } 
    }
}


if (! Object.prototype.create)
{
    Object.prototype.create = function(ptype)
    {
        function O() { }
        var o = new O();
        o.prototype = ptype; 
        return o;
    }
}
