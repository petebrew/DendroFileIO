<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Transforms a v1.2.3 TRiDaS XML file to a v1.2.2 TRiDaS XML file -->

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/> 
  </xsl:copy>
</xsl:template>


<!-- Rename elevation tag to altitude -->
<xsl:template match="elevation">
  <altitude>
    <xsl:apply-templates select="@*|node()"/>
  </altitude>
</xsl:template>

</xsl:stylesheet>