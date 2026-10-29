package br.com.nfmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@With
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    private int id;
    private String inscFed;
    private String razaoSocial;
    private String CEP;
    private String codIBGE;
    private String endNum;
    private String logr;
    private String bairro;
}
