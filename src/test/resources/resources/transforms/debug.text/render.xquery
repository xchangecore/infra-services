xquery version "1.0";
module namespace ns='http://uicds.org/modules/transform/debug.text';

declare namespace NS_PRECIS_STRUCTURE = "http://www.saic.com/precis/2009/06/structures";

declare function ns:render($config as node(), $props as node(),  $workproducts as node()) as item()* {
    fn:concat("This should return text only.",
        " There are ", fn:count($workproducts//NS_PRECIS_STRUCTURE:WorkProduct), " workproducts in the list. ")
};