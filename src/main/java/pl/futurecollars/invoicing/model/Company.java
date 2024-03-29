package pl.futurecollars.invoicing.model;

import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Company implements WithId {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ApiModelProperty(value = "Company id", required = true, example = "54")
  private Long id;

  @ApiModelProperty(value = "Tax Identification Number", required = true, example = "443-112-33-11")
  private String taxIdentificationNumber;

  @ApiModelProperty(value = "Company address", required = true, example = "ul. Jana Pawła II 21/37, 34-100 Wadowice")
  private String address;

  @ApiModelProperty(value = "Company name", required = true, example = "Manufaktura Faktur sp. z o.o.")
  private String name;

  @ApiModelProperty(value = "Pension insurance amount", required = true, example = "1328.75")
  private BigDecimal pensionInsurance = BigDecimal.ZERO;

  @ApiModelProperty(value = "Health insurance amount", required = true, example = "458.34")
  private BigDecimal healthInsurance = BigDecimal.ZERO;

}
