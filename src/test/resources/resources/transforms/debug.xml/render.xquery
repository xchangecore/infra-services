xquery version "1.0";
module namespace ns='http://uicds.org/modules/transform/debug.xml';

declare function ns:render($config as node(), $props as node(),  $workproducts as node()) as item()* {
    <debug>
        <config>
            {$config}
        </config>

        <properties>
            {$props}
        </properties>

        <data>
            {$workproducts}
        </data>
    </debug>
};