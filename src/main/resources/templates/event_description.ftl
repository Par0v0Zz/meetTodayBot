<#if accepted??>
<#if accepted>ACCEPTED

<#else>CANCELLED

</#if><#else><#if pendingOther>PENDING

</#if></#if>${date.format("EEEE, d MMM\nHH:mm '('zzz')'", zone)}

${creatorLink}<#if creatorHost>\*</#if> <#if creatorOk?? && creatorOk>🗸</#if>, ${partnerLink}<#if !creatorHost>\*</#if> <#if partnerOk?? && partnerOk>🗸</#if>

\* default host