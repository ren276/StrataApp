package com.example.strata.data.repository

import com.example.strata.data.model.Template
import com.example.strata.ui.editor.EditorViewModel

object MockTemplateRepository {

    val templates = listOf(
        // -- POPULAR --
        Template(
            id = "urban_dash",
            name = "Urban Dash",
            category = "Popular",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB4aCbSm2dgpGLTkscN65PTRZQ88s0OAZnbOYk-0OoKioLjlduCKp5gNToYMJ2JIGQ8D9O3RmwEysc5z0sb0vnRZJozRV6OONwTC59B_jTtBhEdh3lyQ547cMzNcCHrDoZDoqE8L1OCUxnrkBDI0lc34TCHeFB5XxEKYFEFDw_RIkqLJC8av70PXcYnXhrO5ZwcDYnJjmNgr6aqHxzbGLiCExx0y1BaJPCPEwIGiC_2frvZomC4n69e9ENHv0NeAAc2_OS9GXyzPyg",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB4aCbSm2dgpGLTkscN65PTRZQ88s0OAZnbOYk-0OoKioLjlduCKp5gNToYMJ2JIGQ8D9O3RmwEysc5z0sb0vnRZJozRV6OONwTC59B_jTtBhEdh3lyQ547cMzNcCHrDoZDoqE8L1OCUxnrkBDI0lc34TCHeFB5XxEKYFEFDw_RIkqLJC8av70PXcYnXhrO5ZwcDYnJjmNgr6aqHxzbGLiCExx0y1BaJPCPEwIGiC_2frvZomC4n69e9ENHv0NeAAc2_OS9GXyzPyg",
            elements = defaultElements("URBAN DASH")
        ),
        Template(
            id = "alpine_peak",
            name = "Alpine Peak",
            category = "Popular",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB642JKdxuhfdMxkJ_fwXP--nRy52YKLfpPAreH6v90S302j2TxGnMvcCzXuZGj9z1xevVZSBw7z4GL99cIwO2Q37IvWmnz14RrOCdjkSn3rH-T7bpZ85fZRkL8iTDKum8f6kgk7CFLrEHDqgxpUl_OachULod7WzEooapMzZ7GZxkhsLZorEksnyMQXJ7Si9woadKtmCD9AdvImYngpj4WhQvAxqxJ4fDlxt6CaasAOm1IcrtAYqludJ9AE6FWDE3wY0ZsmOSvUhU",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB642JKdxuhfdMxkJ_fwXP--nRy52YKLfpPAreH6v90S302j2TxGnMvcCzXuZGj9z1xevVZSBw7z4GL99cIwO2Q37IvWmnz14RrOCdjkSn3rH-T7bpZ85fZRkL8iTDKum8f6kgk7CFLrEHDqgxpUl_OachULod7WzEooapMzZ7GZxkhsLZorEksnyMQXJ7Si9woadKtmCD9AdvImYngpj4WhQvAxqxJ4fDlxt6CaasAOm1IcrtAYqludJ9AE6FWDE3wY0ZsmOSvUhU",
            elements = defaultElements("ALPINE PEAK")
        ),

        // -- SCENIC VIEWS --
        Template(
            id = "scenic_lake",
            name = "Mountain Lake",
            category = "Scenic",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAtQe5K1mkO4hfI3yw89JX9PXIlux4yICVRkp9pY8pAo8zeRisF8qxpNDfYlBHAKpaS28LlTDQFs1YHgL044esdy1S4cTTKlNetAvCHnzJ75cugOkLnjXfIfMlJ2qJbN2A4xnQIWb_Y2g-Y89MeC8QyBLEri-CuPr6Nq_bRFFtg2nDjwHnTEFu4uT2nfnWkXl61fAN_NjEa5f1fplzDJ9cez6krALR2P7l29vnsR_Vx54wZrw9KHF_vJQ4dmUwnNqqDhUA-RBKtb1s",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAtQe5K1mkO4hfI3yw89JX9PXIlux4yICVRkp9pY8pAo8zeRisF8qxpNDfYlBHAKpaS28LlTDQFs1YHgL044esdy1S4cTTKlNetAvCHnzJ75cugOkLnjXfIfMlJ2qJbN2A4xnQIWb_Y2g-Y89MeC8QyBLEri-CuPr6Nq_bRFFtg2nDjwHnTEFu4uT2nfnWkXl61fAN_NjEa5f1fplzDJ9cez6krALR2P7l29vnsR_Vx54wZrw9KHF_vJQ4dmUwnNqqDhUA-RBKtb1s",
            elements = defaultElements("")
        ),
        Template(
            id = "scenic_trail",
            name = "Forest Trail",
            category = "Scenic",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB0VPfoXPDAy9YrBQCJuwz_1PXaWnheFyfx7JFVhekpB4uSlP9xo8c0L9mFhxx8Etoc7NO4lswIwsGgro-1ko5Lt7-jvqVU-eLBnVd1zUjZNzARJvIIPfatXjX1fVrdcCHQbeeH4_r0WwMjrAwUnr3wahsRIE5exEsbsBJZ176-p0mNiDx28g8TdTIuu002eTNQLyOS4elObiNNUu7kqMXfjtZRLuvxoxTh1E4JwNyQxB4JMk39oyVzMhksc37eF7mFgigetJV6wzU",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB0VPfoXPDAy9YrBQCJuwz_1PXaWnheFyfx7JFVhekpB4uSlP9xo8c0L9mFhxx8Etoc7NO4lswIwsGgro-1ko5Lt7-jvqVU-eLBnVd1zUjZNzARJvIIPfatXjX1fVrdcCHQbeeH4_r0WwMjrAwUnr3wahsRIE5exEsbsBJZ176-p0mNiDx28g8TdTIuu002eTNQLyOS4elObiNNUu7kqMXfjtZRLuvxoxTh1E4JwNyQxB4JMk39oyVzMhksc37eF7mFgigetJV6wzU",
            elements = defaultElements("")
        ),
        Template(
            id = "scenic_coast",
            name = "Coastline",
            category = "Scenic",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuApprOKFnVgB33ermDVK4vdgtO3moHpfsttK6pyWNuEi98rfV0swG-sRH0evgj--Ggj-WJ2IQQ0mLRA7iNEO1JYJV8O1lkuVNA8SH8E-QfMupWRfEKvKy47Sl1XwCbyDpqtx2mCIA0SC42bUpKpfs-UwUosY50UpHvyoF609OAQwo7AwnZ0btn3ks6bvf35p9I14j8LC1VmO0rXQyB0QY1qSy97Qu2TA_9JKSOT_Jk4nlgBcMbV6r2nLSHKVgjJeWeRyKbRRFmJ6OU",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuApprOKFnVgB33ermDVK4vdgtO3moHpfsttK6pyWNuEi98rfV0swG-sRH0evgj--Ggj-WJ2IQQ0mLRA7iNEO1JYJV8O1lkuVNA8SH8E-QfMupWRfEKvKy47Sl1XwCbyDpqtx2mCIA0SC42bUpKpfs-UwUosY50UpHvyoF609OAQwo7AwnZ0btn3ks6bvf35p9I14j8LC1VmO0rXQyB0QY1qSy97Qu2TA_9JKSOT_Jk4nlgBcMbV6r2nLSHKVgjJeWeRyKbRRFmJ6OU",
            elements = defaultElements("")
        ),
        Template(
            id = "scenic_sunset",
            name = "Golden Meadow",
            category = "Scenic",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDL3oKK-A1XhXOLIqADglDgfx_2zFL5eLeN5YKrPkl22CvgDf-sF9eiWM5cqdEIBApEcfqy-cEgOLITBiwfYen4cZIRO--ES2alvrvKV9J_BBc_bscGLYeEkFszilIAaqMWvFzUS0l7_F6AXSY6-JOaQ_IkaX1ZyGykURHMvlKjtXbtOlutM2azO7XoE1VJjcNgS_j6iLGYQhbnmOS4fhtiW-ghBci93IaWPrdUtdh0R449pnXavj0rWjN2ziIN7Mg3WiXzpR_4AGw",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDL3oKK-A1XhXOLIqADglDgfx_2zFL5eLeN5YKrPkl22CvgDf-sF9eiWM5cqdEIBApEcfqy-cEgOLITBiwfYen4cZIRO--ES2alvrvKV9J_BBc_bscGLYeEkFszilIAaqMWvFzUS0l7_F6AXSY6-JOaQ_IkaX1ZyGykURHMvlKjtXbtOlutM2azO7XoE1VJjcNgS_j6iLGYQhbnmOS4fhtiW-ghBci93IaWPrdUtdh0R449pnXavj0rWjN2ziIN7Mg3WiXzpR_4AGw",
            elements = defaultElements("")
        ),

        // -- MINIMALIST -- (Will handle gradients natively or pass identifying names)
        Template(
            id = "min_noir",
            name = "Noir",
            category = "Minimalist",
            thumbnailUrl = "gradient_noir", // Handled in UI
            backgroundUrl = "gradient_noir", // Handled in UI
            elements = defaultElements("")
        ),
        Template(
            id = "min_aura",
            name = "Aura",
            category = "Minimalist",
            thumbnailUrl = "gradient_aura",
            backgroundUrl = "gradient_aura",
            elements = defaultElements("")
        ),
        Template(
            id = "min_deep",
            name = "Deep",
            category = "Minimalist",
            thumbnailUrl = "gradient_deep",
            backgroundUrl = "gradient_deep",
            elements = defaultElements("")
        ),
        Template(
            id = "min_fresh",
            name = "Fresh",
            category = "Minimalist",
            thumbnailUrl = "gradient_fresh",
            backgroundUrl = "gradient_fresh",
            elements = defaultElements("")
        ),
        Template(
            id = "min_glow",
            name = "Glow",
            category = "Minimalist",
            thumbnailUrl = "gradient_glow",
            backgroundUrl = "gradient_glow",
            elements = defaultElements("")
        ),
        Template(
            id = "min_mist",
            name = "Mist",
            category = "Minimalist",
            thumbnailUrl = "gradient_mist",
            backgroundUrl = "gradient_mist",
            elements = defaultElements("")
        ),
        
        // -- ACTIVITIES --
        Template(
            id = "act_yoga",
            name = "Yoga Studio",
            category = "Activities",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6tmxwwP2N-5PbOcb15B97_lfqEVSnO6wXrmDftAhmOplVXwMmIP0jmJQRFnk1toad51MI-Pr6a73KYUasUqM4cftuXQSQvX962cIdwyQdSdGNUOgSLq6dwTrXIxt7DuBh1UCv-xHYDVZ4rli-P-W6Mw-bbLNydyAou1YhHZSNhPVdpV2FlH8fwUJFJ0y5j9D82RzSNUGlVrzNqwyZUlRsqpTWTpz8hRxKjCFQkSBqHNiuMyBtGj7XhhTWyaJCZcIa3S7eq3nX37I",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6tmxwwP2N-5PbOcb15B97_lfqEVSnO6wXrmDftAhmOplVXwMmIP0jmJQRFnk1toad51MI-Pr6a73KYUasUqM4cftuXQSQvX962cIdwyQdSdGNUOgSLq6dwTrXIxt7DuBh1UCv-xHYDVZ4rli-P-W6Mw-bbLNydyAou1YhHZSNhPVdpV2FlH8fwUJFJ0y5j9D82RzSNUGlVrzNqwyZUlRsqpTWTpz8hRxKjCFQkSBqHNiuMyBtGj7XhhTWyaJCZcIa3S7eq3nX37I",
            elements = defaultElements("")
        ),
        Template(
            id = "act_iron",
            name = "Iron Gym",
            category = "Activities",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAMwgE2wfkMPwNB7V8ur1-bLNEwydt0M4UXJeVYlHaEFeTmKY34D_uZFfctlbAjyIDnGmmUEpMB3rm3x4GChlsXpDKYAEux-X9z8R2MnXm-5nWXLsI4pdWedm-z58PW0GEQVHNg5kgWb9JeawLk6EX8Rv7Kw8bXdnAJfpGfs-wXUww8QdaL4dn1EVybLoxKcJCT4wDBua-bsQMFWjoFMVbITcy6dwQzsAbO5JL9XOxjgXRs-nNsuFMuEn5Wmx39Tlc8uta5hknm8Pc",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAMwgE2wfkMPwNB7V8ur1-bLNEwydt0M4UXJeVYlHaEFeTmKY34D_uZFfctlbAjyIDnGmmUEpMB3rm3x4GChlsXpDKYAEux-X9z8R2MnXm-5nWXLsI4pdWedm-z58PW0GEQVHNg5kgWb9JeawLk6EX8Rv7Kw8bXdnAJfpGfs-wXUww8QdaL4dn1EVybLoxKcJCT4wDBua-bsQMFWjoFMVbITcy6dwQzsAbO5JL9XOxjgXRs-nNsuFMuEn5Wmx39Tlc8uta5hknm8Pc",
            elements = defaultElements("")
        ),
        Template(
            id = "act_tempo",
            name = "High Tempo",
            category = "Activities",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCQN_QVMxlRihcvWFV5o2MpzhKOJfsaLzPP4Y1_4ppNSl7oYQ3cDNZdzxr-Mk5WuxlNGVr6PkhqFb8IK2TT-h6_zlCGZatHrhmXRDCzO-m0Y5PIm6S38i_5VdihQZNuDiCoUKHECD2x1V1yz4bI9rXQ3YQuqjd9CD5kSSYf2DOb8UREIcItNrN54qMRj6F3OO5-NGQD7jOJUvzsexSzxus-FSsfqJqLjukaIDufFUzGq5C6x0ZXrntBWrJq5eYDli8KqpUEVeaNS2Y",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCQN_QVMxlRihcvWFV5o2MpzhKOJfsaLzPP4Y1_4ppNSl7oYQ3cDNZdzxr-Mk5WuxlNGVr6PkhqFb8IK2TT-h6_zlCGZatHrhmXRDCzO-m0Y5PIm6S38i_5VdihQZNuDiCoUKHECD2x1V1yz4bI9rXQ3YQuqjd9CD5kSSYf2DOb8UREIcItNrN54qMRj6F3OO5-NGQD7jOJUvzsexSzxus-FSsfqJqLjukaIDufFUzGq5C6x0ZXrntBWrJq5eYDli8KqpUEVeaNS2Y",
            elements = defaultElements("")
        ),
        Template(
            id = "act_city",
            name = "City Night",
            category = "Activities",
            thumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDMcIO79_uGPFN6ACo1oJaW-orMQQjrCdGv9dLDTaW74E13sz3tU2wL1IFhH8gB04rlDgyzVzZBscG6YWLdAN3jPQWyhcrHB-HGgxl6cZVYKZ_HxLB_8caGBcFK1ihd7_mkASkubK3ihfUn9qO3MS3eRHlHyn5oeiEATveEqXZLxUO3pbzyPJkh65UtUG_UV76P-nWCgE4LxLXZAD5QiMluJgPfqrjH6zDhjIY-mZfwM7YU5LxZxoCnGjFYs1UbqnaHbw9L8_Ub6CI",
            backgroundUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDMcIO79_uGPFN6ACo1oJaW-orMQQjrCdGv9dLDTaW74E13sz3tU2wL1IFhH8gB04rlDgyzVzZBscG6YWLdAN3jPQWyhcrHB-HGgxl6cZVYKZ_HxLB_8caGBcFK1ihd7_mkASkubK3ihfUn9qO3MS3eRHlHyn5oeiEATveEqXZLxUO3pbzyPJkh65UtUG_UV76P-nWCgE4LxLXZAD5QiMluJgPfqrjH6zDhjIY-mZfwM7YU5LxZxoCnGjFYs1UbqnaHbw9L8_Ub6CI",
            elements = defaultElements("")
        )
    )

    private fun defaultElements(titleText: String): List<EditorViewModel.EditorElement> {
        return listOf(
            EditorViewModel.EditorElement.Text(
                text = titleText,
                x = 0.5f, y = 0.15f,
                color = 0xFFFFFFFF,
                fontSize = 42,
                font = EditorViewModel.FontType.BEBAS_NEUE,
                textAlign = EditorViewModel.TextAlign.CENTER
            ),
            EditorViewModel.EditorElement.Data(
                type = EditorViewModel.DataType.DISTANCE,
                value = "0.00", label = "KM",
                x = 0.5f, y = 0.8f,
                color = 0xFFFA6000,
                fontSize = 64,
                font = EditorViewModel.FontType.BEBAS_NEUE,
                textAlign = EditorViewModel.TextAlign.CENTER
            )
        )
    }

    fun getTemplate(id: String): Template? = templates.find { it.id == id }
}
