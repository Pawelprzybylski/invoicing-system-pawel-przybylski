package pl.futurecollars.invoicing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceEntry {

  @JsonIgnore
  @ApiModelProperty(value = "Invoice number (assigned by user)", required = true, example = "2020/03/08/0000001")
  private int id;

  @ApiModelProperty(value = "Product/service description", required = true, example = "Lego 21309 Saturn V")
  private String description;

  @ApiModelProperty(value = "Number of items", required = true, example = "9")
  private int quantity;

  @Builder.Default
  @ApiModelProperty(value = "Product/service net price", required = true, example = "425.09")
  private BigDecimal netPrice = new BigDecimal(0);

  @ApiModelProperty(value = "Product/service tax value", required = true, example = "97.77")
  private BigDecimal vatValue = new BigDecimal(0);

  @ApiModelProperty(value = "Tax rate", required = true)
  private Vat vatRate;

  @ApiModelProperty(value = "Car this expense is related to, empty if expense is not related to car")
  private Car expenseRelatedToCar;

}
