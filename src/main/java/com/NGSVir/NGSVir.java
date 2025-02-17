package com.NGSVir;

import com.NGSVir.configSet.InputParameterAttributes;
import com.NGSVir.configSet.ProcessInputParameters;
import com.NGSVir.coverageCalculator.MainCalculator;
import com.NGSVir.sqldbOperate.exonMode.ExonModelData;

/**
 * @author Benchen Ye
 * @create 2024-11--21:36
 */
public class NGSVir {
    public static void main(String[] args) {

        ProcessInputParameters.processInputParameter(args);
        // exonMode for exon
        if(InputParameterAttributes.analysis_type.equals("exon")){
            ExonModelData.getExonModelData(InputParameterAttributes.tbl_name,
                    InputParameterAttributes.analysis_type,
                    InputParameterAttributes.biotype);
        }
        MainCalculator.mainCalculator();
    }

}
