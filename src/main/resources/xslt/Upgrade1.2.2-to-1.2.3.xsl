<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Transforms a v1.2.2 TRiDaS XML file to a v1.2.3 TRiDaS XML file -->


<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/> 
  </xsl:copy>
</xsl:template>

<!-- Rename altitude tag to elevation -->
<xsl:template match="altitude">
  <elevation>
    <xsl:apply-templates select="@*|node()"/>
  </elevation>
</xsl:template>

</xsl:stylesheet>