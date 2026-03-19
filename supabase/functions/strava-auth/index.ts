import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { code, refresh_token, grant_type } = await req.json()
    
    // Read the secret securely from Supabase Environment Variables
    const clientId = Deno.env.get('STRAVA_CLIENT_ID')
    const clientSecret = Deno.env.get('STRAVA_CLIENT_SECRET')

    if (!clientId || !clientSecret) {
      throw new Error("Missing Strava credentials in backend")
    }

    const stravaUrl = "https://www.strava.com/oauth/token"
    const params = new URLSearchParams()
    params.append('client_id', clientId)
    params.append('client_secret', clientSecret)
    params.append('grant_type', grant_type)

    if (grant_type === 'authorization_code') {
      params.append('code', code)
    } else if (grant_type === 'refresh_token') {
      params.append('refresh_token', refresh_token)
    } else {
      throw new Error("Invalid grant_type")
    }

    const response = await fetch(stravaUrl, {
      method: 'POST',
      body: params,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    const data = await response.json()

    if (!response.ok) {
      console.error("Strava Error:", data)
      return new Response(JSON.stringify({ error: data }), {
        status: response.status,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      })
    }

    return new Response(JSON.stringify(data), {
      status: 200,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' }
    })

  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 400,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' }
    })
  }
})
