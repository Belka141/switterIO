<#import "parts/common.ftl" as c>
<#import "parts/login.ftl" as l>

<@c.page>
    Add new user
<#--    <#if message??>${message}<#else></#if>-->
    ${message}
    <@l.login "/registration" />
</@c.page>