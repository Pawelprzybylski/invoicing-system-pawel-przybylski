package pl.futurecollars.invoicing.model;

import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceEntry {

  @ApiModelProperty(value = "Product/service description", required = true, example = "Dell X12 v3")
  private String description;

  @ApiModelProperty(value = "Number of items", required = true, example = "85")
  private int quantity;

  @ApiModelProperty(value = "Product/service net price", required = true, example = "1857.15")
  private BigDecimal price;

  @ApiModelProperty(value = "Product/service tax value", required = true, example = "187.45")
  private BigDecimal vatValue;

  @ApiModelProperty(value = "Tax rate", required = true)
  private Vat vatRate;

}

